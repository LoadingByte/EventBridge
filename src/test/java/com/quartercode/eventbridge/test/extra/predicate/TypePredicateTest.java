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

package com.quartercode.eventbridge.test.extra.predicate;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.extra.predicate.TypePredicate;
import com.quartercode.eventbridge.test.DummyEvents.EmptyEvent1;
import com.quartercode.eventbridge.test.DummyEvents.EmptyEvent2;
import com.quartercode.eventbridge.test.DummyEvents.EmptyEvent3Base;
import com.quartercode.eventbridge.test.DummyEvents.EmptyEvent3Extends2;

@RunWith (Parameterized.class)
public class TypePredicateTest {

    @Parameters
    public static Collection<Object[]> data() {

        List<Object[]> data = new ArrayList<>();

        data.add(new Object[] { new EmptyEvent1(), EmptyEvent1.class, true });
        data.add(new Object[] { new EmptyEvent1(), EmptyEvent2.class, false });
        data.add(new Object[] { new EmptyEvent1(), EmptyEvent3Base.class, false });
        data.add(new Object[] { new EmptyEvent1(), EmptyEvent3Extends2.class, false });

        data.add(new Object[] { new EmptyEvent2(), EmptyEvent1.class, false });
        data.add(new Object[] { new EmptyEvent2(), EmptyEvent2.class, true });
        data.add(new Object[] { new EmptyEvent2(), EmptyEvent3Base.class, false });
        data.add(new Object[] { new EmptyEvent1(), EmptyEvent3Extends2.class, false });

        data.add(new Object[] { new EmptyEvent3Extends2(), EmptyEvent1.class, false });
        data.add(new Object[] { new EmptyEvent3Extends2(), EmptyEvent2.class, true });
        data.add(new Object[] { new EmptyEvent3Extends2(), EmptyEvent3Base.class, true });
        data.add(new Object[] { new EmptyEvent3Extends2(), EmptyEvent3Extends2.class, true });

        return data;
    }

    private final Event                  event;
    private final Class<? extends Event> type;
    private final boolean                result;

    public TypePredicateTest(Event event, Class<? extends Event> type, boolean result) {

        this.event = event;
        this.type = type;
        this.result = result;
    }

    @Test
    public void testMatches() {

        TypePredicate<Event> predicate = new TypePredicate<>(type);
        assertEquals("Result for event of type " + event.getClass().getName() + " and matching type " + type.getName(), result, predicate.test(event));
    }

}
