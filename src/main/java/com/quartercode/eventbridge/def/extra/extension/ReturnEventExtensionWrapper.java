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

package com.quartercode.eventbridge.def.extra.extension;

import com.quartercode.eventbridge.basic.EventBase;
import com.quartercode.eventbridge.basic.EventPredicateBase;
import com.quartercode.eventbridge.basic.EventUtils;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.EventPredicate;

/**
 * The return event extension wrapper wraps around an {@link Event} and stores a {@code requestId}.
 * It is used by the {@link DefaultReturnEventExtensionRequester} and {@link DefaultReturnEventExtensionReturner} classes.
 * 
 * @see ReturnEventExtensionWrapperPredicate
 */
public class ReturnEventExtensionWrapper extends EventBase {

    private static final long serialVersionUID = -7635621226887580047L;

    private final Event       event;
    private final long        requestId;
    private final boolean     request;

    /**
     * Creates a new return event extension wrapper and wraps around the given {@link Event}.
     * 
     * @param event The event the new wrapper wraps around.
     * @param requestId The value {@code requestId} field that is stored by the handler.
     *        It is used by the {@link DefaultReturnEventExtensionRequester} to recognize return events.
     * @param request Whether the new event is a request ({@code true}) or a return ({@code false}).
     */
    public ReturnEventExtensionWrapper(Event event, long requestId, boolean request) {

        this.event = event;
        this.requestId = requestId;
        this.request = request;
    }

    /**
     * Returns the {@link Event} the wrapper wraps around.
     * 
     * @return The wrapped event.
     */
    public Event getEvent() {

        return event;
    }

    /**
     * Returns the request id that is stored by the handler.
     * It is used by the {@link DefaultReturnEventExtensionRequester} to recognize return events.
     * 
     * @return The stored {@code requestId}.
     */
    public long getRequestId() {

        return requestId;
    }

    /**
     * Returns whether the event is a request ({@code true}) or a return ({@code false}).
     * 
     * @return Whether the event is a request or a return.
     */
    public boolean isRequest() {

        return request;
    }

    /**
     * The return event extension wrapper predicate tests the {@link Event} which is wrapped by a {@link ReturnEventExtensionWrapper}.
     * For doing that, it takes another {@link EventPredicate} which takes care of testing the wrapped event.
     * 
     * @see ReturnEventExtensionWrapper
     */
    public static class ReturnEventExtensionWrapperPredicate extends EventPredicateBase<ReturnEventExtensionWrapper> {

        private static final long       serialVersionUID = 8044226686785560676L;

        private final EventPredicate<?> wrappedPredicate;

        /**
         * Creates a new return event extension wrapper predicate.
         * 
         * @param wrappedPredicate The {@link EventPredicate} that tests the {@link Event} which is wrapped inside a {@link ReturnEventExtensionWrapper}.
         */
        public ReturnEventExtensionWrapperPredicate(EventPredicate<?> wrappedPredicate) {

            this.wrappedPredicate = wrappedPredicate;
        }

        @Override
        public boolean test(ReturnEventExtensionWrapper event) {

            Event wrappedEvent = event.getEvent();

            if (wrappedEvent != null) {
                return EventUtils.tryTest(wrappedPredicate, wrappedEvent);
            } else {
                return false;
            }
        }

    }

}
