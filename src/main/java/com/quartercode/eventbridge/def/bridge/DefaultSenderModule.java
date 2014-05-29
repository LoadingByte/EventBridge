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

package com.quartercode.eventbridge.def.bridge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.BridgeConnectorException;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.SenderModule;
import com.quartercode.eventbridge.channel.Channel;
import com.quartercode.eventbridge.channel.ChannelInvocation;
import com.quartercode.eventbridge.def.channel.DefaultChannel;

/**
 * The default default implementation of the {@link DefaultSenderModule} interface.
 * 
 * @see SenderModule
 */
public class DefaultSenderModule extends BridgeModuleBase implements SenderModule {

    private static final Logger                        LOGGER                  = LoggerFactory.getLogger(DefaultSenderModule.class);

    private final Channel<GlobalSendInterceptor>       globalSendChannel       = new DefaultChannel<>(GlobalSendInterceptor.class);
    private final Channel<LocalHandlerSendInterceptor> localHandlerSendChannel = new DefaultChannel<>(LocalHandlerSendInterceptor.class);
    private final Channel<ConnectorSendInterceptor>    connectorSendChannel    = new DefaultChannel<>(ConnectorSendInterceptor.class);

    /**
     * Creates a new default sender module.
     * 
     * @param parent The parent {@link Bridge} that uses the sender module.
     */
    public DefaultSenderModule(Bridge parent) {

        super(parent);

        globalSendChannel.addInterceptor(new FinalGlobalSendInterceptor(), 0);
        localHandlerSendChannel.addInterceptor(new FinalLocalHandlerSendInterceptor(), 0);
        connectorSendChannel.addInterceptor(new FinalConnectorSendInterceptor(), 0);
    }

    @Override
    public Channel<GlobalSendInterceptor> getGlobalSendChannel() {

        return globalSendChannel;
    }

    @Override
    public Channel<LocalHandlerSendInterceptor> getLocalHandlerSendChannel() {

        return localHandlerSendChannel;
    }

    @Override
    public Channel<ConnectorSendInterceptor> getConnectorSendChannel() {

        return connectorSendChannel;
    }

    @Override
    public void send(Event event) {

        ChannelInvocation<GlobalSendInterceptor> invocation = globalSendChannel.invoke();
        invocation.next().send(invocation, event);
    }

    private class FinalGlobalSendInterceptor implements GlobalSendInterceptor {

        @Override
        public void send(ChannelInvocation<GlobalSendInterceptor> invocation, Event event) {

            invokeLocalHandlerSendChannel(event);
            invokeConnectorSendChannel(event);

            invocation.next().send(invocation, event);
        }

        private void invokeLocalHandlerSendChannel(Event event) {

            ChannelInvocation<LocalHandlerSendInterceptor> newInvocation = localHandlerSendChannel.invoke();
            newInvocation.next().send(newInvocation, event);
        }

        private void invokeConnectorSendChannel(Event event) {

            for (BridgeConnector connector : getParent().getConnectors()) {
                ChannelInvocation<ConnectorSendInterceptor> newInvocation = connectorSendChannel.invoke();
                newInvocation.next().send(newInvocation, connector, event);
            }
        }

    }

    private class FinalLocalHandlerSendInterceptor implements LocalHandlerSendInterceptor {

        @Override
        public void send(ChannelInvocation<LocalHandlerSendInterceptor> invocation, Event event) {

            getParent().handle(null, event);

            invocation.next().send(invocation, event);
        }

    }

    /*
     * This class is static because it doesn't need a reference to DefaultSenderModule.
     * -> Better performance
     */
    private static class FinalConnectorSendInterceptor implements ConnectorSendInterceptor {

        @Override
        public void send(ChannelInvocation<ConnectorSendInterceptor> invocation, BridgeConnector connector, Event event) {

            try {
                connector.send(event);
            } catch (BridgeConnectorException e) {
                LOGGER.error("Can't send event '{}' through bridge connector '{}'", event, connector, e);
            }

            invocation.next().send(invocation, connector, event);
        }

    }

}
