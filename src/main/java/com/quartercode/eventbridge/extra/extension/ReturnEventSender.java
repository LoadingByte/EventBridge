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

import com.quartercode.eventbridge.bridge.Event;

/**
 * Return event senders are used by {@link RequestEventHandler}s for sending return {@link Event}s back.
 * The sender provides a single {@link #send(Event)} method which sends the provided event back to the sender of the request.
 */
public interface ReturnEventSender {

    /**
     * Sends the given return {@link Event} back to the sender of the request event which triggered the {@link RequestEventHandler} call.
     * 
     * @param event The return event which should be sent back to the requester.
     */
    public void send(Event event);

}
