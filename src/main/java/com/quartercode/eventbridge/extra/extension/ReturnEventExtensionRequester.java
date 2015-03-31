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

package com.quartercode.eventbridge.extra.extension;

import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.BridgeModule;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.module.EventHandler;
import com.quartercode.eventbridge.channel.Channel;
import com.quartercode.eventbridge.channel.ChannelInvocation;

/**
 * The return event extension requester is the request part of the return event extension family.
 * That extension family can be used to do fast and resource-efficient request-response communication.
 * This part in particular can send request {@link Event}s and handle incoming return events.<br>
 * <br>
 * Since the return event extension requester is a {@link BridgeModule}, it can be added to a bridge as follows:
 * 
 * <pre>
 * Bridge bridge = ...
 * ReturnEventExtensionRequester extension = ...
 * bridge.addModule(extension);
 * </pre>
 * 
 * Please note that the extension also can be removed from a bridge:
 * 
 * <pre>
 * Bridge bridge = ...
 * ReturnEventExtensionRequester extension = ...
 * bridge.addModule(extension);
 * ...
 * bridge.removeModule(bridge.getModule(ReturnEventExtensionRequester.class));
 * </pre>
 * 
 * @see Bridge
 * @see Event
 * @see ReturnEventExtensionReturner
 */
public interface ReturnEventExtensionRequester extends BridgeModule {

    /**
     * Returns the {@link Channel} which sends request {@link Event}s over the parent {@link Bridge}.
     * It is invoked by the {@link #sendRequest(Event, EventHandler)} method.
     * 
     * @return The channel which sends request events.
     */
    public Channel<RequestSendInterceptor> getRequestSendChannel();

    /**
     * Returns the {@link Channel} which delivers return {@link Event}s to a specific return {@link EventHandler}.
     * The channel is invoked by a hook which is put into the bridge's handler module.
     * 
     * @return The channel which delivers return events to return handlers.
     */
    public Channel<ReturnHandleInterceptor> getReturnHandleChannel();

    /**
     * Sends the given request {@link Event} through the request send channel and calls the given return {@link EventHandler} when the return event arrives.
     * If no return event is catched, the return handler is not invoked.
     * 
     * @param request The request event which should be sent through the request send channel ({@link #getRequestSendChannel()}).
     *        It is probably sent through the {@link Bridge#send(Event)} method.
     * @param returnHandler The return handler which is called when the return event arrives.
     */
    public void sendRequest(Event request, EventHandler<?> returnHandler);

    /**
     * The interceptor which is used in the request send channel of a {@link ReturnEventExtensionRequester}.
     * 
     * @see ReturnEventExtensionRequester#getRequestSendChannel()
     */
    public static interface RequestSendInterceptor {

        /**
         * Intercepts the sending process of the given {@link Event} over the parent {@link Bridge}.
         * 
         * @param invocation The {@link ChannelInvocation} object for the current invocation chain.
         * @param request The request event which is transported through the channel.
         *        It probably will be sent through the {@link Bridge#send(Event)} method.
         * @param returnHandler The return handler which should called when the return event of the given request event arrives.
         */
        public void sendRequest(ChannelInvocation<RequestSendInterceptor> invocation, Event request, EventHandler<?> returnHandler);

    }

    /**
     * The interceptor which is used in the return handle channel of a {@link ReturnEventExtensionRequester}.
     * 
     * @see ReturnEventExtensionRequester#getReturnHandleChannel()
     */
    public static interface ReturnHandleInterceptor {

        /**
         * Intercepts the delivery process of the given return {@link Event} to the given return {@link EventHandler}.
         * 
         * @param invocation The {@link ChannelInvocation} object for the current invocation chain.
         * @param returnEvent The return event which is transported through the channel.
         *        It should be delivered the given return handler.
         * @param source The {@link BridgeConnector} which received the return event.
         *        May be {@code null} if the given event was sent from the same bridge as the one which is handling it.
         * @param returnHandler The return handler the given return event is delivered to.
         */
        public void handleReturn(ChannelInvocation<ReturnHandleInterceptor> invocation, Event returnEvent, BridgeConnector source, EventHandler<?> returnHandler);

    }

}
