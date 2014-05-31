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

package com.quartercode.eventbridge.bridge.module;

import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.BridgeModule;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.EventHandler;
import com.quartercode.eventbridge.channel.Channel;
import com.quartercode.eventbridge.channel.ChannelInvocation;

/**
 * The {@link BridgeModule} which takes care of delivering {@link Event}s to the {@link Bridge#handle(BridgeConnector, Event)} method of the module's bridge.
 * It hooks into the {@link SenderModule}'s channel for diverting sent events into its local handler send channel ({@link #getChannel()}).
 * The module allows to handle sent events locally using {@link EventHandler}s.
 * 
 * @see Event
 * @see Bridge#handle(BridgeConnector, Event)
 */
public interface LocalHandlerSenderModule extends BridgeModule {

    /**
     * Returns the {@link Channel} which delivers {@link Event}s to the {@link Bridge#handle(BridgeConnector, Event)} method of the module's bridge.
     * The last interceptor calls the that method.
     * The channel is invoked by the {@link #send(Event)} method.
     * 
     * @return The channel which delivers events to the handle method of the module's bridge.
     */
    public Channel<LocalHandlerSendInterceptor> getChannel();

    /**
     * Sends the given {@link Event} through the local handler send channel ({@link #getChannel()}).
     * 
     * @param event The event which should be sent through the local handler send channel.
     */
    public void send(Event event);

    /**
     * The interceptor which is used in the local handler send channel of a {@link LocalHandlerSenderModule}.
     * 
     * @see LocalHandlerSenderModule#getChannel()
     */
    public static interface LocalHandlerSendInterceptor {

        /**
         * Intercepts the delivery process of the given {@link Event} to the {@link Bridge#handle(BridgeConnector, Event)} method
         * of the {@link LocalHandlerSenderModule}'s bridge.
         * 
         * @param invocation The {@link ChannelInvocation} object for the current invocation chain.
         * @param event The event which is transported through the channel.
         *        It should be delivered to the bridge's handle method.
         */
        public void send(ChannelInvocation<LocalHandlerSendInterceptor> invocation, Event event);

    }

}
