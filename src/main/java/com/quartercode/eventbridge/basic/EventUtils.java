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

package com.quartercode.eventbridge.basic;

import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.EventPredicate;
import com.quartercode.eventbridge.bridge.module.EventHandler;

/**
 * The event utils class provides some utility methods that are related to {@link Event}s and their environment ({@link EventPredicate}, {@link EventHandler} etc.).
 * 
 * @see Event
 * @see EventPredicate
 * @see EventHandler
 */
public class EventUtils {

    /**
     * Lets the given {@link EventPredicate} test the given {@link Event} and returns the result.
     * If the type of the event doesn't match the generic parameter of the predicate, this method returns {@code false}.
     * 
     * @param predicate The event predicate that should test the given event.
     * @param event The event that should be tested by the given event predicate.
     * @return Whether the generic parameter of the given predicate matches the event type and returns {@code true} for the given event.
     */
    public static <T extends Event> boolean tryTest(EventPredicate<T> predicate, Event event) {

        try {
            @SuppressWarnings ("unchecked")
            T castedEvent = (T) event;
            return predicate.test(castedEvent);
        } catch (ClassCastException e) {
            return false;
        }
    }

    /**
     * Lets the given {@link EventHandler} handle the given {@link Event}.
     * If the type of the event doesn't match the generic parameter of the handler, this method does nothing.
     * 
     * @param handler The event handler that should handle the given event.
     * @param event The event that should be handled by the given event handler.
     */
    public static <T extends Event> void tryHandle(EventHandler<T> handler, Event event) {

        try {
            @SuppressWarnings ("unchecked")
            T castedEvent = (T) event;
            handler.handle(castedEvent);
        } catch (ClassCastException e) {
            // Do nothing
        }
    }

    private EventUtils() {

    }

}
