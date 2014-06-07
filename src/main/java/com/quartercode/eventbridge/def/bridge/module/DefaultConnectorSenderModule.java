/*
 * This file is part of EventBridge.
 * Copyright (c) 2014 QuarterCode <http://www.quartercode.com/>
 *
 * EventBridge is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * EventBridge is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EventBridge. If not, see <http://www.gnu.org/licenses/>.
 */

package com.quartercode.eventbridge.def.bridge.module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.quartercode.eventbridge.basic.AbstractBridgeModule;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.BridgeConnectorException;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.module.ConnectorSenderModule;
import com.quartercode.eventbridge.bridge.module.SenderModule;
import com.quartercode.eventbridge.bridge.module.SenderModule.SendInterceptor;
import com.quartercode.eventbridge.channel.Channel;
import com.quartercode.eventbridge.channel.ChannelInvocation;
import com.quartercode.eventbridge.def.channel.DefaultChannel;

/**
 * The default default implementation of the {@link ConnectorSenderModule} interface.
 * 
 * @see ConnectorSenderModule
 */
public class DefaultConnectorSenderModule extends AbstractBridgeModule implements ConnectorSenderModule {

    private static final Logger                             LOGGER                       = LoggerFactory.getLogger(DefaultConnectorSenderModule.class);

    private final Channel<GlobalConnectorSendInterceptor>   globalChannel                = new DefaultChannel<>(GlobalConnectorSendInterceptor.class);
    private final Channel<SpecificConnectorSendInterceptor> specificChannel              = new DefaultChannel<>(SpecificConnectorSendInterceptor.class);

    private final SendChannelDivertInterceptor              sendChannelDivertInterceptor = new SendChannelDivertInterceptor();

    /**
     * Creates a new default connector sender module.
     */
    public DefaultConnectorSenderModule() {

        globalChannel.addInterceptor(new LastGlobalConnectorSendInterceptor(), 0);
        specificChannel.addInterceptor(new LastSpecificConnectorSendInterceptor(), 0);
    }

    @Override
    public void add(Bridge bridge) {

        super.add(bridge);

        bridge.getModule(SenderModule.class).getChannel().addInterceptor(sendChannelDivertInterceptor, 50);
    }

    @Override
    public void remove() {

        getBridge().getModule(SenderModule.class).getChannel().removeInterceptor(sendChannelDivertInterceptor);

        super.remove();
    }

    @Override
    public Channel<GlobalConnectorSendInterceptor> getGlobalChannel() {

        return globalChannel;
    }

    @Override
    public Channel<SpecificConnectorSendInterceptor> getSpecificChannel() {

        return specificChannel;
    }

    @Override
    public void send(Event event) {

        ChannelInvocation<GlobalConnectorSendInterceptor> invocation = globalChannel.invoke();
        invocation.next().send(invocation, event);
    }

    private class SendChannelDivertInterceptor implements SendInterceptor {

        @Override
        public void send(ChannelInvocation<SendInterceptor> invocation, Event event) {

            DefaultConnectorSenderModule.this.send(event);

            invocation.next().send(invocation, event);
        }

    }

    private class LastGlobalConnectorSendInterceptor implements GlobalConnectorSendInterceptor {

        @Override
        public void send(ChannelInvocation<GlobalConnectorSendInterceptor> invocation, Event event) {

            for (BridgeConnector connector : getBridge().getConnectors()) {
                invokeSpecificConnectorSendChannel(event, connector);
            }

            invocation.next().send(invocation, event);
        }

        private void invokeSpecificConnectorSendChannel(Event event, BridgeConnector connector) {

            ChannelInvocation<SpecificConnectorSendInterceptor> invocation = specificChannel.invoke();
            invocation.next().send(invocation, event, connector);
        }

    }

    private static class LastSpecificConnectorSendInterceptor implements SpecificConnectorSendInterceptor {

        @Override
        public void send(ChannelInvocation<SpecificConnectorSendInterceptor> invocation, Event event, BridgeConnector connector) {

            try {
                connector.send(event);
            } catch (BridgeConnectorException e) {
                LOGGER.error("Can't send event '{}' through bridge connector '{}'", event, connector, e);
            }

            invocation.next().send(invocation, event, connector);
        }

    }

}
