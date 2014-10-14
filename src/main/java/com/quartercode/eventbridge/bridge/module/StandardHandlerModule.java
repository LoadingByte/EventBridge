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

import java.util.Map;
import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.BridgeModule;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.EventPredicate;
import com.quartercode.eventbridge.channel.Channel;
import com.quartercode.eventbridge.channel.ChannelInvocation;

/**
 * The {@link BridgeModule} which takes care of delivering {@link Event}s to all registered high-level {@link EventHandler}s.
 * It adds a {@link LowLevelHandler} adapter, which invokes the main channel ({@link #getChannel()}), for every high-level event handler.
 * 
 * @see Event
 * @see EventHandler
 */
public interface StandardHandlerModule extends BridgeModule {

    /**
     * Returns all {@link EventHandler}s which are listening for incoming {@link Event}s, along with their {@link EventPredicate} matchers.
     * An event handler should only be invoked if its event predicate returns {@code true} for the event.
     * 
     * @return The event handlers that are listening on the standard handler module.
     */
    public Map<EventHandler<?>, EventPredicate<?>> getHandlers();

    /**
     * Adds the given {@link EventHandler} to the standard handler module.
     * It'll start listening for incoming {@link Event}s that match the given {@link EventPredicate}.
     * 
     * @param handler The new event handler that should start listening on the standard handler module.
     * @param predicate An event predicate that decides which events are passed into the handler.
     */
    public void addHandler(EventHandler<?> handler, EventPredicate<?> predicate);

    /**
     * Removes the given {@link EventHandler} from the standard handler module.
     * It'll stop listening for incoming {@link Event}s.
     * 
     * @param handler The event handler that should stop listening on the standard handler module.
     */
    public void removeHandler(EventHandler<?> handler);

    /**
     * Adds the given {@link ModifyStandardHandlerListListener} that is called when an {@link EventHandler} is added or removed.
     * 
     * @param listener The listener that should be added.
     * @see #addHandler(EventHandler, EventPredicate)
     * @see #removeHandler(EventHandler)
     */
    public void addModifyHandlerListListener(ModifyStandardHandlerListListener listener);

    /**
     * Removes the given {@link ModifyStandardHandlerListListener} that is called when an {@link EventHandler} is added or removed.
     * 
     * @param listener The listener that should be removed.
     * @see #addHandler(EventHandler, EventPredicate)
     * @see #removeHandler(EventHandler)
     */
    public void removeModifyHandlerListListener(ModifyStandardHandlerListListener listener);

    /**
     * Returns the {@link Channel} which delivers {@link Event}s to a specific high-level {@link EventHandler}.
     * 
     * @return The channel which delivers events to a specific event handler.
     */
    public Channel<StandardHandleInterceptor> getChannel();

    /**
     * A modify standard handler list listener is called when an {@link EventHandler} is added to or removed from a {@link StandardHandlerModule}.
     */
    public static interface ModifyStandardHandlerListListener {

        /**
         * This method is invoked when the given {@link EventHandler} is being added to the given {@link StandardHandlerModule}.
         * It is called after the handler is added.
         * 
         * @param handler The event handler that is added to the standard handler module.
         * @param predicate The {@link EventPredicate} matcher that belongs to the handler.
         * @param module The standard handler module the given handler is added to.
         */
        public void onAddHandler(EventHandler<?> handler, EventPredicate<?> predicate, StandardHandlerModule module);

        /**
         * This method is invoked when the given {@link EventHandler} is being removed from the given {@link StandardHandlerModule}.
         * It is called before the handler is removed.
         * 
         * @param handler The event handler that is removed from the standard handler module.
         * @param predicate The {@link EventPredicate} matcher that belonged to the handler.
         * @param module The standard handler module the given handler is removed from.
         */
        public void onRemoveHandler(EventHandler<?> handler, EventPredicate<?> predicate, StandardHandlerModule module);

    }

    /**
     * The interceptor which is used in the standard handle channel of a {@link StandardHandlerModule}.
     * 
     * @see StandardHandlerModule#getChannel()
     */
    public static interface StandardHandleInterceptor {

        /**
         * Intercepts the delivery process of the given {@link Event} to the given {@link EventHandler}.
         * 
         * @param invocation The {@link ChannelInvocation} object for the current invocation chain.
         * @param event The event which is transported through the channel.
         *        It should be delivered the given event handler.
         * @param source The {@link BridgeConnector} which received the event.
         *        May be {@code null} if the handled event was sent from the same bridge as the one which is handling it.
         * @param handler The event handler the given event is delivered to.
         */
        public void handle(ChannelInvocation<StandardHandleInterceptor> invocation, Event event, BridgeConnector source, EventHandler<?> handler);

    }

}
