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
     * Returns the {@link Channel} which delivers {@link Event}s to the handler handle channel of the module.
     * It is invoked by the {@link #handle(Event)} method.
     * The handler handle channel can be accessed with {@link #getHandlerHandleChannel()}.
     * 
     * @return The channel which delivers events to the handler handle channel.
     */
    public Channel<GlobalHandleInterceptor> getGlobalHandleChannel();

    /**
     * Returns the {@link Channel} which delivers {@link Event}s to a specific local {@link EventHandler}.
     * It is invoked by the last interceptor of the global handle channel ({@link #getGlobalHandleChannel()}).
     * 
     * @return The channel which delivers events to a specific local {@link EventHandler}.
     */
    public Channel<HandlerHandleInterceptor> getHandlerHandleChannel();

    /**
     * Sends the given {@link Event} through the handle channel ({@link #getHandlerHandleChannel()}).
     * That channel will deliver the event to all of the bridge's {@link EventHandler}s.
     * 
     * @param event The event that should be sent through the handle.
     */
    public void handle(Event event);

    /**
     * The interceptor which is used in the global handle channel of a {@link HandlerModule}.
     * 
     * @see HandlerModule#getGlobalHandleChannel()
     */
    public static interface GlobalHandleInterceptor {

        /**
         * Intercepts the delivery process of the given {@link Event} to the handler handle channel.
         * 
         * @param invocation The {@link ChannelInvocation} object for the current invocation chain.
         * @param event The event which is delivered to the handler handle channel.
         */
        public void handle(ChannelInvocation<GlobalHandleInterceptor> invocation, Event event);

    }

    /**
     * The interceptor which is used in the handler handle channel of a {@link HandlerModule}.
     * 
     * @see HandlerModule#getHandlerHandleChannel()
     */
    public static interface HandlerHandleInterceptor {

        /**
         * Intercepts the delivery process of the given {@link Event} to the given local {@link EventHandler} of a bridge.
         * 
         * @param invocation The {@link ChannelInvocation} object for the current invocation chain.
         * @param handler The local event handler the given event is delivered to.
         * @param event The event which is delivered to the given local handler.
         */
        public void handle(ChannelInvocation<HandlerHandleInterceptor> invocation, EventHandler<?> handler, Event event);

    }

}
