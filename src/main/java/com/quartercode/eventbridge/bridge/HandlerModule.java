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

import java.util.Map;
import com.quartercode.eventbridge.channel.Channel;
import com.quartercode.eventbridge.channel.ChannelInvocation;

/**
 * The part of a {@link Bridge} which takes care of transporting incoming {@link Event}s to the local {@link EventHandler}s.
 * 
 * @see Bridge
 * @see Event
 * @see EventHandler
 */
public interface HandlerModule extends BridgeModule {

    /**
     * Returns all {@link EventHandler}s which are listening for incoming {@link Event}s, along with their {@link EventPredicate} matchers.
     * An event handler should only be invoked if its event predicate returns {@code true} for the event.
     * 
     * @return The event handlers that are listening on the handle module.
     */
    public Map<EventHandler<?>, EventPredicate<?>> getHandlers();

    /**
     * Adds the given {@link EventHandler} to handle module.
     * It'll start listening for incoming {@link Event}s that match the given {@link EventPredicate}.
     * 
     * @param handler The new event handler that should start listening on the handle module.
     * @param predicate An event predicate that decides which events pass into the handler.
     */
    public void addHandler(EventHandler<?> handler, EventPredicate<?> predicate);

    /**
     * Removes the given {@link EventHandler} from the handle module.
     * It'll stop listening for incoming {@link Event}s.
     * 
     * @param handler The event handler that should stop listening on the handle module.
     */
    public void removeHandler(EventHandler<?> handler);

    /**
     * Adds the given {@link ModifyHandlerListListener} that is called when an {@link EventHandler} is added or removed.
     * 
     * @param listener The listener that should be added.
     * @see #addHandler(EventHandler, EventPredicate)
     * @see #removeHandler(EventHandler)
     */
    public void addModifyHandlerListListener(ModifyHandlerListListener listener);

    /**
     * Removes the given {@link ModifyHandlerListListener} that is called when an {@link EventHandler} is added or removed.
     * 
     * @param listener The listener that should be removed.
     * @see #addHandler(EventHandler, EventPredicate)
     * @see #removeHandler(EventHandler)
     */
    public void removeModifyHandlerListListener(ModifyHandlerListListener listener);

    /**
     * Returns the {@link Channel} which delivers {@link Event}s to the handler handle channel of the module.
     * It is invoked by the {@link #handle(BridgeConnector, Event)} method.
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
     * That channel will deliver the event to all of the handle module's {@link EventHandler}s.
     * 
     * @param source The {@link BridgeConnector} which received the event.
     *        May be {@code null} if the handled event was sent from the same bridge which is handling it.
     * @param event The event that should be sent through the handle.
     */
    public void handle(BridgeConnector source, Event event);

    /**
     * A modify handler list listener is called when an {@link EventHandler} is added to or removed from a {@link HandlerModule}.
     */
    public static interface ModifyHandlerListListener {

        /**
         * This method is invoked when the given {@link EventHandler} is being added to the given {@link HandlerModule}.
         * It is called after the handler is added.
         * 
         * @param handler The event handler that is added to the handler module.
         * @param predicate The {@link EventPredicate} matcher that belongs to the handler.
         * @param handlerModule The handler module the given handler is added to.
         */
        public void onAddHandler(EventHandler<?> handler, EventPredicate<?> predicate, HandlerModule handlerModule);

        /**
         * This method is invoked when the given {@link EventHandler} is being removed from the given {@link HandlerModule}.
         * It is called before the handler is removed.
         * 
         * @param handler The event handler that is removed from the handler module.
         * @param predicate The {@link EventPredicate} matcher that belonged to the handler.
         * @param handlerModule The handler module the given handler is removed from.
         */
        public void onRemoveHandler(EventHandler<?> handler, EventPredicate<?> predicate, HandlerModule handlerModule);

    }

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
         * @param source The {@link BridgeConnector} which received the event.
         *        May be {@code null} if the handled event was sent from the same bridge which is handling it.
         * @param event The event which is delivered to the handler handle channel.
         */
        public void handle(ChannelInvocation<GlobalHandleInterceptor> invocation, BridgeConnector source, Event event);

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
         * @param source The {@link BridgeConnector} which received the event.
         *        May be {@code null} if the handled event was sent from the same bridge which is handling it.
         * @param handler The local event handler the given event is delivered to.
         * @param event The event which is delivered to the given local handler.
         */
        public void handle(ChannelInvocation<HandlerHandleInterceptor> invocation, BridgeConnector source, EventHandler<?> handler, Event event);

    }

}
