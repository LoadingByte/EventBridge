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

package com.quartercode.eventbridge.test.def.extra.extension;

import static org.junit.Assert.assertEquals;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.EventPredicate;
import com.quartercode.eventbridge.def.extra.extension.ReturnEventExtensionWrapper;
import com.quartercode.eventbridge.def.extra.extension.ReturnEventExtensionWrapper.ReturnEventExtensionWrapperPredicate;
import com.quartercode.eventbridge.test.DummyEvents.EmptyEvent1;

public class ReturnEventExtensionWrapperPredicateTest {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    @SuppressWarnings ("unchecked")
    @Test
    public void test() {

        final EventPredicate<Event> truePredicate = context.mock(EventPredicate.class, "truePredicate");
        final EventPredicate<Event> falsePredicate = context.mock(EventPredicate.class, "falsePredicate");

        // @formatter:off
        context.checking(new Expectations() {{

            allowing(truePredicate).test(with(any(Event.class)));
                will(returnValue(true));
                allowing(falsePredicate).test(with(any(Event.class)));
                will(returnValue(false));

        }});
        // @formatter:on

        ReturnEventExtensionWrapperPredicate predicateWithTrue = new ReturnEventExtensionWrapperPredicate(truePredicate);
        ReturnEventExtensionWrapperPredicate predicateWithFalse = new ReturnEventExtensionWrapperPredicate(falsePredicate);
        ReturnEventExtensionWrapper event1 = new ReturnEventExtensionWrapper(new EmptyEvent1(), 0, false);
        ReturnEventExtensionWrapper event2 = new ReturnEventExtensionWrapper(null, 0, false);

        assertEquals("Result of wrappedPredicate = EmptyEvent1 and return value true", true, predicateWithTrue.test(event1));
        assertEquals("Result of wrappedPredicate = EmptyEvent1 and return value false", false, predicateWithFalse.test(event1));
        assertEquals("Result of wrappedPredicate = null and return value true", false, predicateWithTrue.test(event2));
        assertEquals("Result of wrappedPredicate = null and return value false", false, predicateWithFalse.test(event2));
    }

}
