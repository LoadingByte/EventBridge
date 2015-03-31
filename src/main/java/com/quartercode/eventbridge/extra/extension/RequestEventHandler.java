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

import com.quartercode.eventbridge.bridge.Event;

/**
 * Request event handlers are classes that take a request {@link Event} and respond with a return event.
 * They are usually used by the {@link ReturnEventExtensionRequester} class.
 * Since events are immutable, request event handlers may not modify the event.<br>
 * <br>
 * The generic parameter {@code <T>} defines the type of event the request handler can handle.
 * That allows request event handlers to handle specific events without having to perform casts.
 * 
 * @param <T> The type of event that can be handled by the request handler.
 * @see Event
 * @see ReturnEventExtensionRequester
 */
public interface RequestEventHandler<T extends Event> {

    /**
     * Processes the given request {@link Event} and responds with a return event.
     * That return event should be sent using the {@link ReturnEventSender#send(Event)} method of the given return event sender.
     * Since events are immutable, this method may not modify the event.
     * The generic parameter {@code <T>} defines the type of event the request handler can process.
     * 
     * @param request The request event that should be processed by the request handler.
     * @param sender The {@link ReturnEventSender} the return event should be send through.
     */
    public void handle(T request, ReturnEventSender sender);

}
