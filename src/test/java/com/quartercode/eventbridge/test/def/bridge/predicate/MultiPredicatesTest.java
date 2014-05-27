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

package com.quartercode.eventbridge.test.def.bridge.predicate;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.EventPredicate;
import com.quartercode.eventbridge.def.bridge.predicate.MultiPredicates;
import com.quartercode.eventbridge.def.bridge.predicate.TypePredicate;
import com.quartercode.eventbridge.test.def.bridge.DummyEvents.EmptyEvent1;
import com.quartercode.eventbridge.test.def.bridge.DummyEvents.EmptyEvent2;
import com.quartercode.eventbridge.test.def.bridge.DummyEvents.EmptyEvent3Base;
import com.quartercode.eventbridge.test.def.bridge.DummyEvents.EmptyEvent3Extends2;

@RunWith (Parameterized.class)
public class MultiPredicatesTest<T extends Event> {

    @Parameters
    public static Collection<Object[]> data() {

        List<Object[]> data = new ArrayList<>();

        EventPredicate<?>[] predicates = new EventPredicate<?>[] { new TypePredicate<>(EmptyEvent2.class), new TypePredicate<>(EmptyEvent3Base.class) };
        data.add(new Object[] { predicates, new EmptyEvent1(), false, false });
        data.add(new Object[] { predicates, new EmptyEvent2(), true, false });
        data.add(new Object[] { predicates, new EmptyEvent3Extends2(), true, true });

        return data;
    }

    private final EventPredicate<? super T>[] predicates;
    private final T                           event;
    private final boolean                     orResult;
    private final boolean                     andResult;

    public MultiPredicatesTest(EventPredicate<? super T>[] predicates, T event, boolean orResult, boolean andResult) {

        this.predicates = predicates;
        this.event = event;
        this.orResult = orResult;
        this.andResult = andResult;
    }

    @Test
    public void testOr() {

        EventPredicate<T> predicate = MultiPredicates.or(predicates);
        assertEquals("Result for or-linked predicates " + Arrays.toString(predicates) + " with event " + event, orResult, predicate.test(event));
    }

    @Test
    public void testAnd() {

        EventPredicate<T> predicate = MultiPredicates.and(predicates);
        assertEquals("Result for and-linked predicates " + Arrays.toString(predicates) + " with event " + event, andResult, predicate.test(event));
    }

}
