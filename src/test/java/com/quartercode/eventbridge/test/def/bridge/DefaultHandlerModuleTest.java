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

import static com.quartercode.eventbridge.test.ExtraAssert.assertListEquals;
import static org.junit.Assert.assertTrue;
import org.apache.commons.lang3.tuple.Pair;
import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.EventHandler;
import com.quartercode.eventbridge.bridge.EventPredicate;
import com.quartercode.eventbridge.bridge.HandlerModule;
import com.quartercode.eventbridge.bridge.HandlerModule.GlobalHandleInterceptor;
import com.quartercode.eventbridge.bridge.HandlerModule.HandlerHandleInterceptor;
import com.quartercode.eventbridge.bridge.HandlerModule.ModifyHandlerListListener;
import com.quartercode.eventbridge.channel.ChannelInvocation;
import com.quartercode.eventbridge.def.bridge.DefaultHandlerModule;
import com.quartercode.eventbridge.test.DummyEvents.CallableEvent;
import com.quartercode.eventbridge.test.DummyEvents.EmptyEvent1;
import com.quartercode.eventbridge.test.DummyEvents.EmptyEvent2;
import com.quartercode.eventbridge.test.DummyInterceptors.DummyGlobalHandleInterceptor;
import com.quartercode.eventbridge.test.DummyInterceptors.DummyHandlerHandleInterceptor;

public class DefaultHandlerModuleTest {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    private HandlerModule   handlerModule;

    @Before
    public void setUp() {

        handlerModule = new DefaultHandlerModule();
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testHandlerStorage() {

        EventHandler<EmptyEvent1> handler1 = context.mock(EventHandler.class, "handler1");
        EventPredicate<EmptyEvent1> predicate1 = context.mock(EventPredicate.class, "predicate1");
        EventHandler<EmptyEvent2> handler2 = context.mock(EventHandler.class, "handler2");
        EventPredicate<EmptyEvent2> predicate2 = context.mock(EventPredicate.class, "predicate2");
        EventHandler<EmptyEvent2> handler3 = context.mock(EventHandler.class, "handler3");
        EventPredicate<EmptyEvent2> predicate3 = context.mock(EventPredicate.class, "predicate3");

        Pair<EventHandler<EmptyEvent1>, EventPredicate<EmptyEvent1>> pair1 = Pair.of(handler1, predicate1);
        Pair<EventHandler<EmptyEvent2>, EventPredicate<EmptyEvent2>> pair2 = Pair.of(handler2, predicate2);
        Pair<EventHandler<EmptyEvent2>, EventPredicate<EmptyEvent2>> pair3 = Pair.of(handler3, predicate3);

        assertHandlerListEmpty();

        handlerModule.removeHandler(handler1);
        assertHandlerListEmpty();

        handlerModule.addHandler(handler1, predicate1);
        assertListEquals("Handlers that are stored inside the bridge are not correct", handlerModule.getHandlers(), pair1);
        assertListEquals("Handlers that are stored inside the bridge changed on the second retrieval", handlerModule.getHandlers(), pair1);

        handlerModule.addHandler(handler2, predicate2);
        assertListEquals("Handlers that are stored inside the bridge are not correct", handlerModule.getHandlers(), pair1, pair2);
        assertListEquals("Handlers that are stored inside the bridge changed on the second retrieval", handlerModule.getHandlers(), pair1, pair2);

        handlerModule.addHandler(handler3, predicate3);
        assertListEquals("Handlers that are stored inside the bridge are not correct", handlerModule.getHandlers(), pair1, pair2, pair3);
        assertListEquals("Handlers that are stored inside the bridge changed on the second retrieval", handlerModule.getHandlers(), pair1, pair2, pair3);

        handlerModule.removeHandler(handler2);
        assertListEquals("Handlers that are stored inside the bridge are not correct", handlerModule.getHandlers(), pair1, pair3);
        assertListEquals("Handlers that are stored inside the bridge changed on the second retrieval", handlerModule.getHandlers(), pair1, pair3);
    }

    private void assertHandlerListEmpty() {

        assertTrue("There are handlers stored inside the bridge although none were added", handlerModule.getHandlers().isEmpty());
        assertTrue("Handlers that are stored inside the bridge changed on the second retrieval", handlerModule.getHandlers().isEmpty());
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testHandlerStorageRemoveUncheckedCast() {

        EventHandler<EmptyEvent1> handler1 = new EqualsAllHandler<>();
        EventPredicate<EmptyEvent1> predicate1 = context.mock(EventPredicate.class);
        EqualsAllHandler<EmptyEvent2> handler2 = new EqualsAllHandler<>();

        // Add handler 1 with type parameter TestEvent1
        handlerModule.addHandler(handler1, predicate1);

        // Remove handler 2 with type parameter TestEvent2
        // Note that handler 2 is equal to handler 1 while having a different type parameter
        handlerModule.removeHandler(handler2);

        assertTrue("Handler 1 wasn't removed", handlerModule.getHandlers().isEmpty());
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testHandlerStorageListeners() {

        final EventHandler<EmptyEvent1> handler = context.mock(EventHandler.class, "handler");
        final EventPredicate<EmptyEvent1> predicate = context.mock(EventPredicate.class, "predicate");
        final ModifyHandlerListListener listener = context.mock(ModifyHandlerListListener.class);

        // @formatter:off
        context.checking(new Expectations() {{

            final Sequence handlerCalls = context.sequence("handlerCalls");
            oneOf(listener).onAddHandler(handler, predicate, handlerModule); inSequence(handlerCalls);
            oneOf(listener).onRemoveHandler(handler, predicate, handlerModule); inSequence(handlerCalls);

        }});
        // @formatter:on

        // Calls with listener
        handlerModule.addModifyHandlerListListener(listener);
        handlerModule.addHandler(handler, predicate);
        handlerModule.removeHandler(handler);

        // Calls without listener
        handlerModule.removeModifyHandlerListListener(listener);
        handlerModule.addHandler(handler, predicate);
        handlerModule.removeHandler(handler);
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testHandle() {

        final BridgeConnector source = context.mock(BridgeConnector.class);

        final EmptyEvent1 regularEvent = new EmptyEvent1();
        final EmptyEvent2 otherEvent = new EmptyEvent2();

        final GlobalHandleInterceptor globalInterceptor = context.mock(GlobalHandleInterceptor.class);
        handlerModule.getGlobalHandleChannel().addInterceptor(new DummyGlobalHandleInterceptor(globalInterceptor), 1);

        final HandlerHandleInterceptor handlerInterceptor = context.mock(HandlerHandleInterceptor.class);
        handlerModule.getHandlerHandleChannel().addInterceptor(new DummyHandlerHandleInterceptor(handlerInterceptor), 1);

        final EventHandler<EmptyEvent1> handler = context.mock(EventHandler.class);
        final EventPredicate<Event> predicate = context.mock(EventPredicate.class);
        handlerModule.addHandler(handler, predicate);

        // @formatter:off
        context.checking(new Expectations() {{

            allowing(predicate).test(regularEvent);
                will(returnValue(true));
            allowing(predicate).test(otherEvent);
                will(returnValue(false));

            final Sequence handleChain = context.sequence("handleChain");
            // Correct event
            oneOf(globalInterceptor).handle(with(any(ChannelInvocation.class)), with(source), with(regularEvent)); inSequence(handleChain);
            oneOf(handlerInterceptor).handle(with(any(ChannelInvocation.class)), with(source), with(handler), with(regularEvent));
            oneOf(handler).handle(regularEvent); inSequence(handleChain);
            // Other event
            oneOf(globalInterceptor).handle(with(any(ChannelInvocation.class)), with(source), with(otherEvent)); inSequence(handleChain);

        }});
        // @formatter:on

        handlerModule.handle(source, regularEvent);

        // Test with wrongly typed event
        handlerModule.handle(source, otherEvent);
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testHandleWrongTypeInPredicate() {

        @SuppressWarnings ("serial")
        final EventPredicate<CallableEvent> predicate = new EventPredicate<CallableEvent>() {

            @Override
            public boolean test(CallableEvent event) {

                // Provoke a ClassCastException
                event.call();
                return true;
            }

        };

        EventHandler<EmptyEvent1> handler = context.mock(EventHandler.class);
        handlerModule.addHandler(handler, predicate);

        // Expect the HandlerModule to suppress the resulting ClassCastException
        handlerModule.handle(null, new EmptyEvent1());
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testHandleWrongTypeInHandler() {

        EventHandler<CallableEvent> handler = new EventHandler<CallableEvent>() {

            @Override
            public void handle(CallableEvent event) {

                // Provoke a ClassCastException
                event.call();
            }

        };
        final EventPredicate<Event> predicate = context.mock(EventPredicate.class);
        handlerModule.addHandler(handler, predicate);

        // @formatter:off
        context.checking(new Expectations() {{

            allowing(predicate).test(with(any(Event.class)));
                will(returnValue(true));

        }});
        // @formatter:on

        // Expect the HandlerModule to suppress the resulting ClassCastException
        handlerModule.handle(null, new EmptyEvent1());
    }

    private static class EqualsAllHandler<T extends Event> implements EventHandler<T> {

        @Override
        public boolean equals(Object obj) {

            return true;
        }

        @Override
        public void handle(T event) {

            // Not used
        }

    }

}
