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

package com.quartercode.eventbridge.bridge;

import com.quartercode.eventbridge.channel.Channel;
import com.quartercode.eventbridge.channel.ChannelInvocation;

/**
 * The part of a {@link Bridge} which takes care of transporting incoming {@link Event}s to the local {@link EventHandler}s.
 * 
 * @see Bridge
 * @see Event
 * @see EventHandler
 */
public interface HandlerModule {

    /**
     * Returns the {@link Channel} which delivers {@link Event}s to all local {@link EventHandler}s.
     * It is invoked by the {@link #handle(Event)} method.
     * 
     * @return The channel which delivers events to the local {@link EventHandler}s.
     */
    public Channel<HandleInterceptor> getHandleChannel();

    /**
     * Sends the given {@link Event} through the handle channel ({@link #getHandleChannel()}).
     * That channel will deliver the event to all of the bridge's {@link EventHandler}s.
     * 
     * @param event The event that should be sent through the handle.
     */
    public void handle(Event event);

    /**
     * The interceptor which is used in the handle channel of a {@link HandlerModule}.
     * 
     * @see HandlerModule#getHandleChannel()
     */
    public static interface HandleInterceptor {

        /**
         * Intercepts the delivery process of the given {@link Event} to the local {@link EventHandler}s of a bridge.
         * 
         * @param invocation The {@link ChannelInvocation} object for the current invocation chain.
         * @param event The event which is delivered to all local handlers.
         */
        public void handle(ChannelInvocation<HandleInterceptor> invocation, Event event);

    }

}
