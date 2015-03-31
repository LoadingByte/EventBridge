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

import com.quartercode.eventbridge.bridge.Event;

/**
 * High-level event handlers are classes that take an {@link Event} and do something depending on the event.
 * Since events are immutable, event handlers may not modify the event.<br>
 * <br>
 * The generic parameter {@code <T>} defines the type of event the handler can handle.
 * That allows event handlers to handle specific events without having to perform casts.
 * 
 * @param <T> The type of event that can be handled by the handler.
 * @see Event
 */
public interface EventHandler<T extends Event> {

    /**
     * Processes the given {@link Event} and does something depending on the event.
     * Since events are immutable, this method may not modify the event.
     * The generic parameter {@code <T>} defines the type of event the handler can process.
     * 
     * @param event The event that should be processed by the handler.
     */
    public void handle(T event);

}
