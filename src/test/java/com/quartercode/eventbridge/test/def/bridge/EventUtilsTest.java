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

package com.quartercode.eventbridge.test.def.bridge;

import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.eventbridge.bridge.EventHandler;
import com.quartercode.eventbridge.bridge.EventPredicate;
import com.quartercode.eventbridge.def.bridge.EventUtils;
import com.quartercode.eventbridge.test.def.bridge.DummyEvents.CallableEvent;
import com.quartercode.eventbridge.test.def.bridge.DummyEvents.EmptyEvent1;

public class EventUtilsTest {

    @Before
    public void setUp() {

    }

    @Test
    public void testTryTest() {

        EventHandler<CallableEvent> handler = new EventHandler<CallableEvent>() {

            @Override
            public void handle(CallableEvent event) {

                event.call();
            }

        };

        CallableEvent regularEvent = new CallableEvent();
        EventUtils.tryHandle(handler, regularEvent);
        assertTrue("Regular event wasn't handled correct", regularEvent.isCalled());

        // Wrong event type; EventUtils.tryHandle should catch the resulting ClassCastException
        EventUtils.tryHandle(handler, new EmptyEvent1());
    }

    @Test
    public void testTryHandle() {

        @SuppressWarnings ("serial")
        EventPredicate<CallableEvent> predicate = new EventPredicate<CallableEvent>() {

            @Override
            public boolean test(CallableEvent event) {

                event.call();
                return true;
            }

        };

        CallableEvent regularEvent = new CallableEvent();
        assertTrue("Regular event wasn't tested correct", EventUtils.tryTest(predicate, regularEvent));
        assertTrue("Regular event wasn't tested correct", regularEvent.isCalled());

        // Wrong event type; EventUtils.tryTest should catch the resulting ClassCastException
        EventUtils.tryTest(predicate, new EmptyEvent1());
    }

}
