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

package com.quartercode.eventbridge.extra.extension;

import java.util.Map;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.BridgeModule;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.EventPredicate;
import com.quartercode.eventbridge.channel.Channel;
import com.quartercode.eventbridge.channel.ChannelInvocation;

/**
 * The return event extension returner is the return part of the return event extension family.
 * That extension family can be used to do fast and resource-efficient request-response communication.
 * This part in particular can deliver incoming request {@link Event}s to registered {@link RequestEventHandler}s.
 * Those request handlers then send the actual return event.<br>
 * <br>
 * Since the return event extension returner is a {@link BridgeModule}, it can be added to a bridge as follows:
 * 
 * <pre>
 * Bridge bridge = ...
 * ReturnEventExtensionReturner extension = ...
 * bridge.addModule(extension);
 * </pre>
 * 
 * Please note that the extension also can be removed from a bridge:
 * 
 * <pre>
 * Bridge bridge = ...
 * ReturnEventExtensionReturner extension = ...
 * bridge.addModule(extension);
 * ...
 * bridge.removeModule(bridge.getModule(ReturnEventExtensionReturner.class));
 * </pre>
 * 
 * @see Bridge
 * @see Event
 * @see RequestEventHandler
 * @see ReturnEventExtensionRequester
 */
public interface ReturnEventExtensionReturner extends BridgeModule {

    /**
     * Returns all {@link RequestEventHandler}s which are listening for incoming request {@link Event}s, along with their {@link EventPredicate} matchers.
     * A request event handler should only be invoked if its event predicate returns {@code true} for the request event.
     * 
     * @return The request event handlers that are listening on the return event extension returner.
     */
    public Map<RequestEventHandler<?>, EventPredicate<?>> getRequestHandlers();

    /**
     * Adds the given {@link RequestEventHandler} to the return event extension returner.
     * It'll start listening for incoming request {@link Event}s that match the given {@link EventPredicate}.
     * 
     * @param requestHandler The new request event handler that should start listening on the return event extension returner.
     * @param predicate An event predicate that decides which events pass into the request handler.
     */
    public void addRequestHandler(RequestEventHandler<?> requestHandler, EventPredicate<?> predicate);

    /**
     * Removes the given {@link RequestEventHandler} from the return event extension returner.
     * It'll stop listening for incoming request {@link Event}s.
     * 
     * @param requestHandler The request event handler that should stop listening on the return event extension returner.
     */
    public void removeRequestHandler(RequestEventHandler<?> requestHandler);

    /**
     * Adds the given {@link ModifyRequestHandlerListListener} that is called when a {@link RequestEventHandler} is added or removed.
     * 
     * @param listener The listener that should be added.
     * @see #addRequestHandler(RequestEventHandler, EventPredicate)
     * @see #removeRequestHandler(RequestEventHandler)
     */
    public void addModifyRequestHandlerListListener(ModifyRequestHandlerListListener listener);

    /**
     * Removes the given {@link ModifyRequestHandlerListListener} that is called when a {@link RequestEventHandler} is added or removed.
     * 
     * @param listener The listener that should be removed.
     * @see #addRequestHandler(RequestEventHandler, EventPredicate)
     * @see #removeRequestHandler(RequestEventHandler)
     */
    public void removeModifyRequestHandlerListListener(ModifyRequestHandlerListListener listener);

    /**
     * Returns the {@link Channel} which delivers request {@link Event}s to a specific {@link RequestEventHandler}.
     * 
     * @return The channel which delivers request events to a specific request event handler.
     */
    public Channel<RequestHandleInterceptor> getRequestHandleChannel();

    /**
     * A modify request handler list listener is called when a {@link RequestEventHandler} is added to or removed from a {@link ReturnEventExtensionReturner}.
     */
    public static interface ModifyRequestHandlerListListener {

        /**
         * This method is invoked when the given {@link RequestEventHandler} is being added to the given {@link ReturnEventExtensionReturner}.
         * It is called after the request handler was added.
         * 
         * @param requestHandler The request event handler that is added to the return event extension returner.
         * @param predicate The {@link EventPredicate} matcher that belongs to the request handler.
         * @param extension The return event extension returner the given request handler is added to.
         */
        public void onAddRequestHandler(RequestEventHandler<?> requestHandler, EventPredicate<?> predicate, ReturnEventExtensionReturner extension);

        /**
         * This method is invoked when the given {@link RequestEventHandler} is being removed from the given {@link ReturnEventExtensionReturner}.
         * It is called before the handler is removed.
         * 
         * @param requestHandler The request event handler that is removed from the return event extension returner.
         * @param predicate The {@link EventPredicate} matcher that belonged to the request handler.
         * @param extension The return event extension returner the given request handler is removed from.
         */
        public void onRemoveRequestHandler(RequestEventHandler<?> requestHandler, EventPredicate<?> predicate, ReturnEventExtensionReturner extension);

    }

    /**
     * The interceptor which is used in the request handle channel of a {@link ReturnEventExtensionReturner}.
     * 
     * @see ReturnEventExtensionReturner#getRequestHandleChannel()
     */
    public static interface RequestHandleInterceptor {

        /**
         * Intercepts the delivery process of the given request {@link Event} to the given {@link RequestEventHandler}.
         * 
         * @param invocation The {@link ChannelInvocation} object for the current invocation chain.
         * @param request The request event which is transported through the channel.
         *        It should be delivered the given request event handler.
         * @param source The {@link BridgeConnector} which received the request event.
         *        May be {@code null} if the handled event was sent from the same bridge as the one which is handling it.
         * @param requestHandler The request event handler the request given event is delivered to.
         * @param returnSender A {@link ReturnEventSender} that can be used by the request handler for sending its return event back.
         */
        public void handleRequest(ChannelInvocation<RequestHandleInterceptor> invocation, Event request, BridgeConnector source, RequestEventHandler<?> requestHandler, ReturnEventSender returnSender);

    }

}
