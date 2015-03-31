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

package com.quartercode.eventbridge.test.def.bridge.module;

import static com.quartercode.eventbridge.test.ExtraActions.storeArgument;
import static com.quartercode.eventbridge.test.ExtraAssert.assertListEquals;
import static com.quartercode.eventbridge.test.ExtraAssert.assertMapEquals;
import static com.quartercode.eventbridge.test.ExtraMatchers.aLowLevelHandlerWithThePredicate;
import static org.junit.Assert.assertTrue;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.commons.lang3.tuple.Pair;
import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.EventPredicate;
import com.quartercode.eventbridge.bridge.module.EventHandler;
import com.quartercode.eventbridge.bridge.module.EventHandlerExceptionCatcher;
import com.quartercode.eventbridge.bridge.module.LowLevelHandler;
import com.quartercode.eventbridge.bridge.module.LowLevelHandlerModule;
import com.quartercode.eventbridge.bridge.module.StandardHandlerModule.ModifyStandardExceptionCatcherListListener;
import com.quartercode.eventbridge.bridge.module.StandardHandlerModule.ModifyStandardHandlerListListener;
import com.quartercode.eventbridge.bridge.module.StandardHandlerModule.StandardHandleExceptionInterceptor;
import com.quartercode.eventbridge.bridge.module.StandardHandlerModule.StandardHandleInterceptor;
import com.quartercode.eventbridge.channel.ChannelInvocation;
import com.quartercode.eventbridge.def.bridge.module.DefaultStandardHandlerModule;
import com.quartercode.eventbridge.test.DummyEvents.CallableEvent;
import com.quartercode.eventbridge.test.DummyEvents.EmptyEvent1;
import com.quartercode.eventbridge.test.DummyEvents.EmptyEvent2;
import com.quartercode.eventbridge.test.DummyInterceptors.DummyStandardHandleExceptionInterceptor;
import com.quartercode.eventbridge.test.DummyInterceptors.DummyStandardHandleInterceptor;

public class DefaultStandardHandlerModuleTest {

    @Rule
    public JUnitRuleMockery              context = new JUnitRuleMockery();

    @Mock
    private Bridge                       bridge;
    @Mock
    private LowLevelHandlerModule        lowLevelHandlerModule;

    private DefaultStandardHandlerModule module;

    @Before
    public void setUp() {

        module = new DefaultStandardHandlerModule();

        // @formatter:off
        context.checking(new Expectations() {{

            allowing(bridge).getModule(LowLevelHandlerModule.class);
                will(returnValue(lowLevelHandlerModule));

        }});
        // @formatter:on

        module.add(bridge);
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testRemove() {

        EventHandler<EmptyEvent1> handler1 = context.mock(EventHandler.class, "handler1");
        EventHandler<EmptyEvent2> handler2 = context.mock(EventHandler.class, "handler2");
        final EventPredicate<EmptyEvent1> predicate1 = context.mock(EventPredicate.class, "predicate1");
        final EventPredicate<EmptyEvent2> predicate2 = context.mock(EventPredicate.class, "predicate2");

        // @formatter:off
        context.checking(new Expectations() {{

            // Add
            oneOf(lowLevelHandlerModule).addHandler(with(aLowLevelHandlerWithThePredicate(predicate1)));
            oneOf(lowLevelHandlerModule).addHandler(with(aLowLevelHandlerWithThePredicate(predicate2)));
            // Automatic removal
            oneOf(lowLevelHandlerModule).removeHandler(with(aLowLevelHandlerWithThePredicate(predicate1)));
            oneOf(lowLevelHandlerModule).removeHandler(with(aLowLevelHandlerWithThePredicate(predicate2)));

        }});
        // @formatter:on

        module.addHandler(handler1, predicate1);
        module.addHandler(handler2, predicate2);

        module.remove();
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testHandlerStorage() {

        EventHandler<EmptyEvent1> handler1 = context.mock(EventHandler.class, "handler1");
        EventHandler<EmptyEvent2> handler2 = context.mock(EventHandler.class, "handler2");
        EventHandler<EmptyEvent2> handler3 = context.mock(EventHandler.class, "handler3");
        final EventPredicate<EmptyEvent1> predicate1 = context.mock(EventPredicate.class, "predicate1");
        final EventPredicate<EmptyEvent2> predicate2 = context.mock(EventPredicate.class, "predicate2");
        final EventPredicate<EmptyEvent2> predicate3 = context.mock(EventPredicate.class, "predicate3");

        Pair<EventHandler<EmptyEvent1>, EventPredicate<EmptyEvent1>> pair1 = Pair.of(handler1, predicate1);
        Pair<EventHandler<EmptyEvent2>, EventPredicate<EmptyEvent2>> pair2 = Pair.of(handler2, predicate2);
        Pair<EventHandler<EmptyEvent2>, EventPredicate<EmptyEvent2>> pair3 = Pair.of(handler3, predicate3);

        // @formatter:off
        context.checking(new Expectations() {{

            final Sequence handlerListModifications = context.sequence("handlerListModifications");
            oneOf(lowLevelHandlerModule).addHandler(with(aLowLevelHandlerWithThePredicate(predicate1))); inSequence(handlerListModifications);
            oneOf(lowLevelHandlerModule).addHandler(with(aLowLevelHandlerWithThePredicate(predicate2))); inSequence(handlerListModifications);
            oneOf(lowLevelHandlerModule).addHandler(with(aLowLevelHandlerWithThePredicate(predicate3))); inSequence(handlerListModifications);
            oneOf(lowLevelHandlerModule).removeHandler(with(aLowLevelHandlerWithThePredicate(predicate2))); inSequence(handlerListModifications);

        }});
        // @formatter:on

        assertHandlerListEmpty();

        module.removeHandler(handler1);
        assertHandlerListEmpty();

        module.addHandler(handler1, predicate1);
        assertMapEquals("Handlers that are stored inside the module are not correct", module.getHandlers(), pair1);
        assertMapEquals("Handlers that are stored inside the module changed on the second retrieval", module.getHandlers(), pair1);

        module.addHandler(handler2, predicate2);
        assertMapEquals("Handlers that are stored inside the module are not correct", module.getHandlers(), pair1, pair2);
        assertMapEquals("Handlers that are stored inside the module changed on the second retrieval", module.getHandlers(), pair1, pair2);

        module.addHandler(handler3, predicate3);
        assertMapEquals("Handlers that are stored inside the module are not correct", module.getHandlers(), pair1, pair2, pair3);
        assertMapEquals("Handlers that are stored inside the module changed on the second retrieval", module.getHandlers(), pair1, pair2, pair3);

        module.removeHandler(handler2);
        assertMapEquals("Handlers that are stored inside the module are not correct", module.getHandlers(), pair1, pair3);
        assertMapEquals("Handlers that are stored inside the module changed on the second retrieval", module.getHandlers(), pair1, pair3);
    }

    private void assertHandlerListEmpty() {

        assertTrue("There are handlers stored inside the module although none were added", module.getHandlers().isEmpty());
        assertTrue("Handlers that are stored inside the module changed on the second retrieval", module.getHandlers().isEmpty());
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testHandlerStorageListeners() {

        final EventHandler<EmptyEvent1> handler = context.mock(EventHandler.class, "handler");
        final EventPredicate<EmptyEvent1> predicate = context.mock(EventPredicate.class, "predicate");
        final ModifyStandardHandlerListListener listener = context.mock(ModifyStandardHandlerListListener.class);

        // @formatter:off
        context.checking(new Expectations() {{

            allowing(lowLevelHandlerModule).addHandler(with(any(LowLevelHandler.class)));
            allowing(lowLevelHandlerModule).removeHandler(with(any(LowLevelHandler.class)));

            final Sequence listenerCalls = context.sequence("listenerCalls");
            oneOf(listener).onAddHandler(handler, predicate, module); inSequence(listenerCalls);
            oneOf(listener).onRemoveHandler(handler, predicate, module); inSequence(listenerCalls);

        }});
        // @formatter:on

        // Calls with listener
        module.addModifyHandlerListListener(listener);
        module.addHandler(handler, predicate);
        module.removeHandler(handler);

        // Calls without listener
        module.removeModifyHandlerListListener(listener);
        module.addHandler(handler, predicate);
        module.removeHandler(handler);
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testCallHandler() {

        final BridgeConnector source = context.mock(BridgeConnector.class);

        final EmptyEvent1 regularEvent = new EmptyEvent1();
        final EmptyEvent2 otherEvent = new EmptyEvent2();

        final EventHandler<Event> handler = context.mock(EventHandler.class, "handler");
        final EventPredicate<Event> predicate = context.mock(EventPredicate.class, "predicate");

        final StandardHandleInterceptor interceptor = context.mock(StandardHandleInterceptor.class);
        module.getHandleChannel().addInterceptor(new DummyStandardHandleInterceptor(interceptor), 1);

        final Mutable<LowLevelHandler> lowLevelHandler = new MutableObject<>();

        // @formatter:off
        context.checking(new Expectations() {{

            allowing(predicate).test(regularEvent);
                will(returnValue(true));
            allowing(predicate).test(otherEvent);
                will(returnValue(false));

            oneOf(lowLevelHandlerModule).addHandler(with(aLowLevelHandlerWithThePredicate(predicate)));
                will(storeArgument(0).in(lowLevelHandler));

            final Sequence handleChain = context.sequence("handleChain");
            // Regular event
            oneOf(interceptor).handle(with(any(ChannelInvocation.class)), with(regularEvent), with(source), with(handler)); inSequence(handleChain);
            oneOf(handler).handle(regularEvent); inSequence(handleChain);
            // Other event
            // Expect the unwanted event to be invoked since the predicate is not tested by the StandardHandlerModule
            // In fact, the predicate is tested by the LowLevelHandlerModule
            oneOf(interceptor).handle(with(any(ChannelInvocation.class)), with(otherEvent), with(source), with(handler)); inSequence(handleChain);
            oneOf(handler).handle(otherEvent); inSequence(handleChain);

        }});
        // @formatter:on

        module.addHandler(handler, predicate);

        lowLevelHandler.getValue().handle(regularEvent, source);
        lowLevelHandler.getValue().handle(otherEvent, source);
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testCallHandlerWrongTypeInPredicate() {

        final EventPredicate<Event> predicate = context.mock(EventPredicate.class);
        final EventHandler<CallableEvent> handler = new EventHandler<CallableEvent>() {

            @Override
            public void handle(CallableEvent event) {

                // Provoke a ClassCastException
                event.call();
            }

        };

        final Mutable<LowLevelHandler> lowLevelHandler = new MutableObject<>();

        // @formatter:off
        context.checking(new Expectations() {{

            oneOf(lowLevelHandlerModule).addHandler(with(aLowLevelHandlerWithThePredicate(predicate)));
                will(storeArgument(0).in(lowLevelHandler));

        }});
        // @formatter:on

        module.addHandler(handler, predicate);

        lowLevelHandler.getValue().handle(new EmptyEvent1(), null);
    }

    @Test
    public void testExceptionCatcherStorage() {

        final EventHandlerExceptionCatcher catcher1 = context.mock(EventHandlerExceptionCatcher.class, "catcher1");
        final EventHandlerExceptionCatcher catcher2 = context.mock(EventHandlerExceptionCatcher.class, "catcher2");
        final EventHandlerExceptionCatcher catcher3 = context.mock(EventHandlerExceptionCatcher.class, "catcher3");

        assertExceptionCatcherListEmpty();

        module.removeExceptionCatcher(catcher1);
        assertExceptionCatcherListEmpty();

        module.addExceptionCatcher(catcher1);
        assertListEquals("Exception catchers that are stored inside the module are not correct", module.getExceptionCatchers(), catcher1);
        assertListEquals("Exception catchers that are stored inside the module changed on the second retrieval", module.getExceptionCatchers(), catcher1);

        module.addExceptionCatcher(catcher2);
        assertListEquals("Exception catchers that are stored inside the module are not correct", module.getExceptionCatchers(), catcher1, catcher2);
        assertListEquals("Exception catchers that are stored inside the module changed on the second retrieval", module.getExceptionCatchers(), catcher1, catcher2);

        module.addExceptionCatcher(catcher3);
        assertListEquals("Exception catchers that are stored inside the module are not correct", module.getExceptionCatchers(), catcher1, catcher2, catcher3);
        assertListEquals("Exception catchers that are stored inside the module changed on the second retrieval", module.getExceptionCatchers(), catcher1, catcher2, catcher3);

        module.removeExceptionCatcher(catcher2);
        assertListEquals("Exception catchers that are stored inside the module are not correct", module.getExceptionCatchers(), catcher1, catcher3);
        assertListEquals("Exception catchers that are stored inside the module changed on the second retrieval", module.getExceptionCatchers(), catcher1, catcher3);
    }

    private void assertExceptionCatcherListEmpty() {

        assertTrue("There are exception catchers stored inside the module although none were added", module.getExceptionCatchers().isEmpty());
        assertTrue("Exception catchers that are stored inside the module changed on the second retrieval", module.getExceptionCatchers().isEmpty());
    }

    @Test
    public void testExceptionCatcherStorageListeners() {

        final EventHandlerExceptionCatcher catcher = context.mock(EventHandlerExceptionCatcher.class, "catcher");
        final ModifyStandardExceptionCatcherListListener listener = context.mock(ModifyStandardExceptionCatcherListListener.class);

        // @formatter:off
        context.checking(new Expectations() {{

            final Sequence listenerCalls = context.sequence("listenerCalls");
            oneOf(listener).onAddCatcher(catcher, module); inSequence(listenerCalls);
            oneOf(listener).onRemoveCatcher(catcher, module); inSequence(listenerCalls);

        }});
        // @formatter:on

        // Calls with listener
        module.addModifyExceptionCatcherListListener(listener);
        module.addExceptionCatcher(catcher);
        module.removeExceptionCatcher(catcher);

        // Calls without listener
        module.removeModifyExceptionCatcherListListener(listener);
        module.addExceptionCatcher(catcher);
        module.removeExceptionCatcher(catcher);
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testExceptionInHandler() {

        final BridgeConnector source = context.mock(BridgeConnector.class);

        final EmptyEvent1 regularEvent = new EmptyEvent1();
        final EmptyEvent2 exceptionEvent = new EmptyEvent2();

        final EventHandler<Event> handler = context.mock(EventHandler.class, "handler");
        final EventPredicate<Event> predicate = context.mock(EventPredicate.class, "predicate");

        final EventHandlerExceptionCatcher catcher1 = context.mock(EventHandlerExceptionCatcher.class, "exceptionCatcher1");
        final EventHandlerExceptionCatcher catcher2 = context.mock(EventHandlerExceptionCatcher.class, "exceptionCatcher2");
        module.addExceptionCatcher(catcher1);
        module.addExceptionCatcher(catcher2);

        final StandardHandleExceptionInterceptor interceptor = context.mock(StandardHandleExceptionInterceptor.class);
        module.getExceptionChannel().addInterceptor(new DummyStandardHandleExceptionInterceptor(interceptor), 1);

        final Mutable<LowLevelHandler> lowLevelHandler = new MutableObject<>();

        // @formatter:off
        context.checking(new Expectations() {{

            allowing(predicate).test(with(any(Event.class)));
                will(returnValue(true));

            oneOf(lowLevelHandlerModule).addHandler(with(aLowLevelHandlerWithThePredicate(predicate)));
                will(storeArgument(0).in(lowLevelHandler));

            final Sequence handleChain = context.sequence("handleChain");
            // Regular event
            oneOf(handler).handle(regularEvent); inSequence(handleChain);
            // Exception event
            final RuntimeException exception = new RuntimeException();
            oneOf(handler).handle(exceptionEvent); inSequence(handleChain);
                will(throwException(exception));
            oneOf(interceptor).handle(with(any(ChannelInvocation.class)), with(exception), with(handler), with(exceptionEvent), with(source)); inSequence(handleChain);
            oneOf(catcher1).handle(exception, handler, exceptionEvent, source); inSequence(handleChain);
            oneOf(catcher2).handle(exception, handler, exceptionEvent, source); inSequence(handleChain);

        }});
        // @formatter:on

        module.addHandler(handler, predicate);

        lowLevelHandler.getValue().handle(regularEvent, source);
        lowLevelHandler.getValue().handle(exceptionEvent, source);
    }

}
