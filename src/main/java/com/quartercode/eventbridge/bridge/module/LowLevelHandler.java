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

import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.EventPredicate;

/**
 * Low-level event handlers are classes that take an unspecified {@link Event} and do something depending on the event.
 * They also receive the {@link BridgeConnector} which received the event for further processing.
 * Since events are immutable, low-level event handlers may not modify the event.<br>
 * <br>
 * Low-level handlers also provide an {@link EventPredicate} which must be true in order for the handler to be invoked.
 * 
 * @see Event
 * @see EventPredicate
 * @see BridgeConnector
 */
public interface LowLevelHandler {

    /**
     * Returns the immutable {@link EventPredicate} that can be used to determine which {@link Event}s can be handled by the low-level handler.
     * Since the predicate is immutable, this method might always return the same object.
     * 
     * @return The event predicate that should be used for determining which events can be handled by the handler.
     */
    public EventPredicate<?> getPredicate();

    /**
     * Processes the given {@link Event} and does something depending on the event.
     * Since events are immutable, this method may not modify the event.
     * 
     * @param event The event that should be processed by the low-level handler.
     * @param source The {@link BridgeConnector} which received the event.
     *        May be {@code null} if the handled event was sent from the same bridge as the one which is handling it.
     */
    public void handle(Event event, BridgeConnector source);

}
