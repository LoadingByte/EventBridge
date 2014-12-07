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

import java.util.List;
import java.util.Map;
import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.BridgeModule;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.EventPredicate;
import com.quartercode.eventbridge.channel.Channel;
import com.quartercode.eventbridge.channel.ChannelInvocation;

/**
 * The {@link BridgeModule} which takes care of delivering {@link Event}s to all registered high-level {@link EventHandler}s.
 * It adds a {@link LowLevelHandler} adapter, which invokes the main channel ({@link #getHandleChannel()}), for every high-level event handler.
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
     * Returns all {@link EventHandlerExceptionCatcher}s which are listening for {@link RuntimeException}s that occur during the handling of an {@link Event}.
     * When a registered {@link EventHandler} throws such an exception, all of these exception catchers are called.
     * 
     * @return The exception catchers that are listening on the standard handler module.
     */
    public List<EventHandlerExceptionCatcher> getExceptionCatchers();

    /**
     * Adds the given {@link EventHandlerExceptionCatcher}s to the standard handler module.
     * It'll start listening for {@link RuntimeException}s that occur during the handling of an {@link Event}.
     * 
     * @param catcher The new exception catcher that should start listening on the standard handler module.
     */
    public void addExceptionCatcher(EventHandlerExceptionCatcher catcher);

    /**
     * Removes the given {@link EventHandlerExceptionCatcher}s from the standard handler module.
     * It'll stop listening for {@link RuntimeException}s that occur during the handling of an {@link Event}.
     * 
     * @param catcher The exception catcher that should stop listening on the standard handler module.
     */
    public void removeExceptionCatcher(EventHandlerExceptionCatcher catcher);

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
     * Adds the given {@link ModifyStandardExceptionCatcherListListener} that is called when an {@link EventHandlerExceptionCatcher} is added or removed.
     * 
     * @param listener The listener that should be added.
     * @see #addExceptionCatcher(EventHandlerExceptionCatcher)
     * @see #removeExceptionCatcher(EventHandlerExceptionCatcher)
     */
    public void addModifyExceptionCatcherListListener(ModifyStandardExceptionCatcherListListener listener);

    /**
     * Removes the given {@link ModifyStandardExceptionCatcherListListener} that is called when an {@link EventHandlerExceptionCatcher} is added or removed.
     * 
     * @param listener The listener that should be removed.
     * @see #addExceptionCatcher(EventHandlerExceptionCatcher)
     * @see #removeExceptionCatcher(EventHandlerExceptionCatcher)
     */
    public void removeModifyExceptionCatcherListListener(ModifyStandardExceptionCatcherListListener listener);

    /**
     * Returns the {@link Channel} which delivers {@link Event}s to a specific high-level {@link EventHandler}.
     * 
     * @return The channel which delivers events to a specific event handler.
     */
    public Channel<StandardHandleInterceptor> getHandleChannel();

    /**
     * Returns the {@link Channel} which delivers {@link RuntimeException}s that occurred during the hanlding of events to all {@link EventHandlerExceptionCatcher}s.
     * 
     * @return The channel which delivers runtime exceptions to all exception catchers.
     */
    public Channel<StandardHandleExceptionInterceptor> getExceptionChannel();

    /**
     * A modify standard handler list listener is called when an {@link EventHandler} is added to or removed from a {@link StandardHandlerModule}.
     */
    public static interface ModifyStandardHandlerListListener {

        /**
         * This method is invoked when the given {@link EventHandler} is being added to the given {@link StandardHandlerModule}.
         * It is called after the handler was added.
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
     * A modify standard exception catcher list listener is called when an {@link EventHandlerExceptionCatcher} is added to or removed from a {@link StandardHandlerModule}.
     */
    public static interface ModifyStandardExceptionCatcherListListener {

        /**
         * This method is invoked when the given {@link EventHandlerExceptionCatcher} is being added to the given {@link StandardHandlerModule}.
         * It is called after the catcher was added.
         * 
         * @param catcher The exception catcher that is added to the standard handler module.
         * @param module The standard handler module the given catcher is added to.
         */
        public void onAddCatcher(EventHandlerExceptionCatcher catcher, StandardHandlerModule module);

        /**
         * This method is invoked when the given {@link EventHandlerExceptionCatcher} is being removed from the given {@link StandardHandlerModule}.
         * It is called before the catcher is removed.
         * 
         * @param catcher The exception catcher that is removed from the standard handler module.
         * @param module The standard handler module the given catcher is removed from.
         */
        public void onRemoveCatcher(EventHandlerExceptionCatcher catcher, StandardHandlerModule module);

    }

    /**
     * The interceptor which is used in the standard handle channel of a {@link StandardHandlerModule}.
     * 
     * @see StandardHandlerModule#getHandleChannel()
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

    /**
     * The interceptor which is used in the standard exception handle channel of a {@link StandardHandlerModule}.
     * 
     * @see StandardHandlerModule#getExceptionChannel()
     */
    public static interface StandardHandleExceptionInterceptor {

        /**
         * Intercepts the delivery process of the given {@link RuntimeException} to all {@link EventHandlerExceptionCatcher}s of the {@link StandardHandlerModule}.
         * 
         * @param invocation The {@link ChannelInvocation} object for the current invocation chain.
         * @param exception The runtime exception which is transported through the channel.
         *        It should be delivered all exception catchers of the module.
         * @param handler The {@link EventHandler} which threw the exception.
         * @param event The {@link Event} which caused the given event handler to throw the given exception on handling.
         * @param source The {@link BridgeConnector} which received the given event.
         *        May be {@code null} if the handled event was sent from the same bridge as the one which is handling it.
         */
        public void handle(ChannelInvocation<StandardHandleExceptionInterceptor> invocation, RuntimeException exception, EventHandler<?> handler, Event event, BridgeConnector source);

    }

}
