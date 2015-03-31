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

package com.quartercode.eventbridge.bridge.module;

import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.BridgeModule;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.channel.Channel;
import com.quartercode.eventbridge.channel.ChannelInvocation;

/**
 * The {@link BridgeModule} which takes care of transporting sent {@link Event}s to other modules that actually deliver the event.
 * These other modules must hook into the module's channel ({@link #getChannel()}) in order to divert the sent events.
 * 
 * @see Event
 */
public interface SenderModule extends BridgeModule {

    /**
     * Returns the {@link Channel} which delivers {@link Event}s to other modules of the {@link Bridge}.
     * These other modules must hook into this channel in order to divert the sent events.
     * The channel is invoked by the {@link #send(Event)} method.
     * 
     * @return The channel which delivers events to other bridge modules.
     */
    public Channel<SendInterceptor> getChannel();

    /**
     * Sends the given {@link Event} through the send channel ({@link #getChannel()}).
     * 
     * @param event The event which should be sent through the send channel.
     */
    public void send(Event event);

    /**
     * The interceptor which is used in the send channel of a {@link SenderModule}.
     * 
     * @see SenderModule#getChannel()
     */
    public static interface SendInterceptor {

        /**
         * Intercepts the delivery process of the given {@link Event} to other modules of the {@link SenderModule}'s {@link Bridge}.
         * 
         * @param invocation The {@link ChannelInvocation} object for the current invocation chain.
         * @param event The event which is transported through the channel.
         *        It should be delivered to other modules of the module's bridge.
         */
        public void send(ChannelInvocation<SendInterceptor> invocation, Event event);

    }

}
