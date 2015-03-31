/*
 * This file is part of EventBridge.
 * Copyright (c) 2014 QuarterCode <http://quartercode.com/>
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

import com.quartercode.eventbridge.basic.AbstractBridgeModule;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.module.LocalHandlerSenderModule;
import com.quartercode.eventbridge.bridge.module.SenderModule;
import com.quartercode.eventbridge.bridge.module.SenderModule.SendInterceptor;
import com.quartercode.eventbridge.channel.Channel;
import com.quartercode.eventbridge.channel.ChannelInvocation;
import com.quartercode.eventbridge.def.channel.DefaultChannel;

/**
 * The default default implementation of the {@link LocalHandlerSenderModule} interface.
 * 
 * @see LocalHandlerSenderModule
 */
public class DefaultLocalHandlerSenderModule extends AbstractBridgeModule implements LocalHandlerSenderModule {

    private final Channel<LocalHandlerSendInterceptor> channel                      = new DefaultChannel<>(LocalHandlerSendInterceptor.class);

    private final SendChannelDivertInterceptor         sendChannelDivertInterceptor = new SendChannelDivertInterceptor();

    /**
     * Creates a new default local handler sender module.
     */
    public DefaultLocalHandlerSenderModule() {

        channel.addInterceptor(new LastLocalHandlerSendInterceptor(), 0);
    }

    @Override
    public void add(Bridge bridge) {

        super.add(bridge);

        bridge.getModule(SenderModule.class).getChannel().addInterceptor(sendChannelDivertInterceptor, 100);
    }

    @Override
    public void remove() {

        getBridge().getModule(SenderModule.class).getChannel().removeInterceptor(sendChannelDivertInterceptor);

        super.remove();
    }

    @Override
    public Channel<LocalHandlerSendInterceptor> getChannel() {

        return channel;
    }

    @Override
    public void send(Event event) {

        ChannelInvocation<LocalHandlerSendInterceptor> invocation = channel.invoke();
        invocation.next().send(invocation, event);
    }

    private class SendChannelDivertInterceptor implements SendInterceptor {

        @Override
        public void send(ChannelInvocation<SendInterceptor> invocation, Event event) {

            DefaultLocalHandlerSenderModule.this.send(event);

            invocation.next().send(invocation, event);
        }

    }

    private class LastLocalHandlerSendInterceptor implements LocalHandlerSendInterceptor {

        @Override
        public void send(ChannelInvocation<LocalHandlerSendInterceptor> invocation, Event event) {

            getBridge().handle(event, null);

            invocation.next().send(invocation, event);
        }

    }

}
