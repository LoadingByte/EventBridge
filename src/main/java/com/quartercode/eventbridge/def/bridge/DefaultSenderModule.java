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

import com.quartercode.eventbridge.basic.AbstractBridgeModule;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.SenderModule;
import com.quartercode.eventbridge.channel.Channel;
import com.quartercode.eventbridge.channel.ChannelInvocation;
import com.quartercode.eventbridge.def.channel.DefaultChannel;

/**
 * The default default implementation of the {@link SenderModule} interface.
 * 
 * @see SenderModule
 */
public class DefaultSenderModule extends AbstractBridgeModule implements SenderModule {

    private final Channel<SendInterceptor> channel = new DefaultChannel<>(SendInterceptor.class);

    /**
     * Creates a new default sender module.
     */
    public DefaultSenderModule() {

    }

    @Override
    public Channel<SendInterceptor> getChannel() {

        return channel;
    }

    @Override
    public void send(Event event) {

        ChannelInvocation<SendInterceptor> invocation = channel.invoke();
        invocation.next().send(invocation, event);
    }

}
