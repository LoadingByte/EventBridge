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

import java.util.List;
import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.BridgeModule;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.EventPredicate;
import com.quartercode.eventbridge.channel.Channel;
import com.quartercode.eventbridge.channel.ChannelInvocation;

/**
 * The {@link BridgeModule} which takes care of delivering {@link Event}s to all registered {@link LowLevelHandler}s.
 * It hooks into the {@link HandlerModule}'s channel for diverting events into its global handle channel ({@link #getGlobalChannel()}).
 * The global channel the splits the invocation into multiple ones for every low-level handler.
 * These specific invocations are transported through the specific handle channel ({@link #getSpecificChannel()}).
 * 
 * @see Event
 * @see LowLevelHandler
 */
public interface LowLevelHandlerModule extends BridgeModule {

    /**
     * Returns all {@link LowLevelHandler}s which are listening for incoming {@link Event}s.
     * A low-level handler should only be invoked if its {@link EventPredicate} ({@link LowLevelHandler#getPredicate()}) returns {@code true} for the event.
     * 
     * @return The low-level handlers that are listening on the low-level handle module.
     */
    public List<LowLevelHandler> getHandlers();

    /**
     * Adds the given {@link LowLevelHandler} to low-level handle module.
     * It'll start listening for incoming {@link Event}s that match its {@link EventPredicate} ({@link LowLevelHandler#getPredicate()}).
     * 
     * @param handler The new low-level handler that should start listening on the low-level handle module.
     */
    public void addHandler(LowLevelHandler handler);

    /**
     * Removes the given {@link LowLevelHandler} from the low-level handle module.
     * It'll stop listening for incoming {@link Event}s.
     * 
     * @param handler The low-level handler that should stop listening on the low-level handle module.
     */
    public void removeHandler(LowLevelHandler handler);

    /**
     * Adds the given {@link ModifyLowLevelHandlerListListener} that is called when a {@link LowLevelHandler} is added or removed.
     * 
     * @param listener The listener that should be added.
     * @see #addHandler(LowLevelHandler)
     * @see #removeHandler(LowLevelHandler)
     */
    public void addModifyHandlerListListener(ModifyLowLevelHandlerListListener listener);

    /**
     * Removes the given {@link ModifyLowLevelHandlerListListener} that is called when a {@link LowLevelHandler} is added or removed.
     * 
     * @param listener The listener that should be removed.
     * @see #addHandler(LowLevelHandler)
     * @see #removeHandler(LowLevelHandler)
     */
    public void removeModifyHandlerListListener(ModifyLowLevelHandlerListListener listener);

    /**
     * Returns the {@link Channel} which delivers {@link Event}s to the specific low-level handle channel ({@link #getSpecificChannel()}).
     * The last interceptor splits an invocation of the channel into multiple ones for every {@link LowLevelHandler}.
     * The channel is invoked by the {@link #handle(Event, BridgeConnector)} method.
     * 
     * @return The channel which delivers events to the specific low-level handle channel.
     */
    public Channel<GlobalLowLevelHandleInterceptor> getGlobalChannel();

    /**
     * Returns the {@link Channel} which delivers {@link Event}s to a specific {@link LowLevelHandler}.
     * It is invoked by the last interceptor of the global low-level handle channel ({@link #getGlobalChannel()}).
     * 
     * @return The channel which delivers events to a specific low-level handler.
     */
    public Channel<SpecificLowLevelHandleInterceptor> getSpecificChannel();

    /**
     * Sends the given {@link Event} through the global low-level handle channel ({@link #getGlobalChannel()}).
     * 
     * @param event The event which should be sent through the global low-level handle channel.
     * @param source The {@link BridgeConnector} which received the event.
     *        May be {@code null} if the handled event was sent from the same bridge as the one which is handling it.
     */
    public void handle(Event event, BridgeConnector source);

    /**
     * A modify low-level handler list listener is called when a {@link LowLevelHandler} is added to or removed from a {@link LowLevelHandlerModule}.
     */
    public static interface ModifyLowLevelHandlerListListener {

        /**
         * This method is invoked when the given {@link LowLevelHandler} is being added to the given {@link LowLevelHandlerModule}.
         * It is called after the handler was added.
         * 
         * @param handler The low-level handler that is added to the low-level handler module.
         * @param module The low-level handler module the given handler is added to.
         */
        public void onAddHandler(LowLevelHandler handler, LowLevelHandlerModule module);

        /**
         * This method is invoked when the given {@link LowLevelHandler} is being removed from the given {@link LowLevelHandlerModule}.
         * It is called before the handler is removed.
         * 
         * @param handler The low-level handler that is removed from the low-level handler module.
         * @param module The low-level handler module the given handler is removed from.
         */
        public void onRemoveHandler(LowLevelHandler handler, LowLevelHandlerModule module);

    }

    /**
     * The interceptor which is used in the global low-level handle channel of an {@link LowLevelHandlerModule}.
     * 
     * @see LowLevelHandlerModule#getGlobalChannel()
     */
    public static interface GlobalLowLevelHandleInterceptor {

        /**
         * Intercepts the delivery process of the given {@link Event} to the specific low-level handle channel of the {@link LowLevelHandlerModule}.
         * 
         * @param invocation The {@link ChannelInvocation} object for the current invocation chain.
         * @param event The event which is transported through the channel.
         *        It should be delivered to the specific low-level handle channel.
         * @param source The {@link BridgeConnector} which received the event.
         *        May be {@code null} if the handled event was sent from the same bridge as the one which is handling it.
         */
        public void handle(ChannelInvocation<GlobalLowLevelHandleInterceptor> invocation, Event event, BridgeConnector source);

    }

    /**
     * The interceptor which is used in the specific low-level handle channel of an {@link LowLevelHandlerModule}.
     * 
     * @see LowLevelHandlerModule#getSpecificChannel()
     */
    public static interface SpecificLowLevelHandleInterceptor {

        /**
         * Intercepts the delivery process of the given {@link Event} to the given {@link LowLevelHandler}.
         * 
         * @param invocation The {@link ChannelInvocation} object for the current invocation chain.
         * @param event The event which is transported through the channel.
         *        It should be delivered the given low-level handler.
         * @param source The {@link BridgeConnector} which received the event.
         *        May be {@code null} if the handled event was sent from the same bridge as the one which is handling it.
         * @param handler The low-level handler the given event is delivered to.
         */
        public void handle(ChannelInvocation<SpecificLowLevelHandleInterceptor> invocation, Event event, BridgeConnector source, LowLevelHandler handler);

    }

}
