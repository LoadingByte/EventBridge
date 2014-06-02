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

import static com.quartercode.eventbridge.test.ExtraActions.storeArgument;
import static com.quartercode.eventbridge.test.ExtraAssert.assertMapEquals;
import static com.quartercode.eventbridge.test.ExtraMatchers.aLowLevelHandlerWithThePredicate;
import static org.junit.Assert.assertTrue;
import java.util.concurrent.atomic.AtomicReference;
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
import com.quartercode.eventbridge.bridge.BridgeConnectorException;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.EventPredicate;
import com.quartercode.eventbridge.bridge.module.LowLevelHandler;
import com.quartercode.eventbridge.bridge.module.LowLevelHandlerModule;
import com.quartercode.eventbridge.channel.ChannelInvocation;
import com.quartercode.eventbridge.def.extra.extension.DefaultReturnEventExtensionReturner;
import com.quartercode.eventbridge.extra.extension.RequestEventHandler;
import com.quartercode.eventbridge.extra.extension.ReturnEventExtensionReturner.ModifyRequestHandlerListListener;
import com.quartercode.eventbridge.extra.extension.ReturnEventExtensionReturner.RequestHandleInterceptor;
import com.quartercode.eventbridge.extra.extension.ReturnEventSender;
import com.quartercode.eventbridge.test.DummyEvents.CallableEvent;
import com.quartercode.eventbridge.test.DummyEvents.EmptyEvent1;
import com.quartercode.eventbridge.test.DummyEvents.EmptyEvent2;
import com.quartercode.eventbridge.test.DummyEvents.EmptyEvent3;

public class DefaultReturnEventExtensionReturnerTest {

    @Rule
    public JUnitRuleMockery                     context = new JUnitRuleMockery();

    @Mock
    private Bridge                              bridge;
    @Mock
    private LowLevelHandlerModule               lowLevelHandlerModule;

    private DefaultReturnEventExtensionReturner extension;

    @Before
    public void setUp() {

        extension = new DefaultReturnEventExtensionReturner();

        // @formatter:off
        context.checking(new Expectations() {{

            allowing(bridge).getModule(LowLevelHandlerModule.class);
                will(returnValue(lowLevelHandlerModule));

        }});
        // @formatter:on

        extension.add(bridge);
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testRemove() {

        RequestEventHandler<EmptyEvent1> handler1 = context.mock(RequestEventHandler.class, "handler1");
        RequestEventHandler<EmptyEvent2> handler2 = context.mock(RequestEventHandler.class, "handler2");
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

        extension.addRequestHandler(handler1, predicate1);
        extension.addRequestHandler(handler2, predicate2);

        extension.remove();
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testRequestHandlerStorage() {

        RequestEventHandler<EmptyEvent1> handler1 = context.mock(RequestEventHandler.class, "handler1");
        RequestEventHandler<EmptyEvent2> handler2 = context.mock(RequestEventHandler.class, "handler2");
        RequestEventHandler<EmptyEvent2> handler3 = context.mock(RequestEventHandler.class, "handler3");
        final EventPredicate<EmptyEvent1> predicate1 = context.mock(EventPredicate.class, "predicate1");
        final EventPredicate<EmptyEvent2> predicate2 = context.mock(EventPredicate.class, "predicate2");
        final EventPredicate<EmptyEvent2> predicate3 = context.mock(EventPredicate.class, "predicate3");

        Pair<RequestEventHandler<EmptyEvent1>, EventPredicate<EmptyEvent1>> pair1 = Pair.of(handler1, predicate1);
        Pair<RequestEventHandler<EmptyEvent2>, EventPredicate<EmptyEvent2>> pair2 = Pair.of(handler2, predicate2);
        Pair<RequestEventHandler<EmptyEvent2>, EventPredicate<EmptyEvent2>> pair3 = Pair.of(handler3, predicate3);

        // @formatter:off
        context.checking(new Expectations() {{

            final Sequence handlerListModifications = context.sequence("handlerListModifications");
            oneOf(lowLevelHandlerModule).addHandler(with(aLowLevelHandlerWithThePredicate(predicate1))); inSequence(handlerListModifications);
            oneOf(lowLevelHandlerModule).addHandler(with(aLowLevelHandlerWithThePredicate(predicate2))); inSequence(handlerListModifications);
            oneOf(lowLevelHandlerModule).addHandler(with(aLowLevelHandlerWithThePredicate(predicate3))); inSequence(handlerListModifications);
            oneOf(lowLevelHandlerModule).removeHandler(with(aLowLevelHandlerWithThePredicate(predicate2))); inSequence(handlerListModifications);

        }});
        // @formatter:on

        assertRequestHandlerListEmpty();

        extension.removeRequestHandler(handler1);
        assertRequestHandlerListEmpty();

        extension.addRequestHandler(handler1, predicate1);
        assertMapEquals("Handlers that are stored inside the module are not correct", extension.getRequestHandlers(), pair1);
        assertMapEquals("Handlers that are stored inside the module changed on the second retrieval", extension.getRequestHandlers(), pair1);

        extension.addRequestHandler(handler2, predicate2);
        assertMapEquals("Handlers that are stored inside the module are not correct", extension.getRequestHandlers(), pair1, pair2);
        assertMapEquals("Handlers that are stored inside the module changed on the second retrieval", extension.getRequestHandlers(), pair1, pair2);

        extension.addRequestHandler(handler3, predicate3);
        assertMapEquals("Handlers that are stored inside the module are not correct", extension.getRequestHandlers(), pair1, pair2, pair3);
        assertMapEquals("Handlers that are stored inside the module changed on the second retrieval", extension.getRequestHandlers(), pair1, pair2, pair3);

        extension.removeRequestHandler(handler2);
        assertMapEquals("Handlers that are stored inside the module are not correct", extension.getRequestHandlers(), pair1, pair3);
        assertMapEquals("Handlers that are stored inside the module changed on the second retrieval", extension.getRequestHandlers(), pair1, pair3);
    }

    private void assertRequestHandlerListEmpty() {

        assertTrue("There are handlers stored inside the module although none were added", extension.getRequestHandlers().isEmpty());
        assertTrue("Handlers that are stored inside the module changed on the second retrieval", extension.getRequestHandlers().isEmpty());
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testRequestHandlerStorageListeners() {

        final RequestEventHandler<EmptyEvent1> handler = context.mock(RequestEventHandler.class, "handler");
        final EventPredicate<EmptyEvent1> predicate = context.mock(EventPredicate.class, "predicate");
        final ModifyRequestHandlerListListener listener = context.mock(ModifyRequestHandlerListListener.class);

        // @formatter:off
        context.checking(new Expectations() {{

            allowing(lowLevelHandlerModule).addHandler(with(any(LowLevelHandler.class)));
            allowing(lowLevelHandlerModule).removeHandler(with(any(LowLevelHandler.class)));

            final Sequence listenerCalls = context.sequence("listenerCalls");
            oneOf(listener).onAddRequestHandler(handler, predicate, extension); inSequence(listenerCalls);
            oneOf(listener).onRemoveRequestHandler(handler, predicate, extension); inSequence(listenerCalls);

        }});
        // @formatter:on

        // Calls with listener
        extension.addModifyRequestHandlerListListener(listener);
        extension.addRequestHandler(handler, predicate);
        extension.removeRequestHandler(handler);

        // Calls without listener
        extension.removeModifyRequestHandlerListListener(listener);
        extension.addRequestHandler(handler, predicate);
        extension.removeRequestHandler(handler);
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testCallRequestHandler() {

        final BridgeConnector source = context.mock(BridgeConnector.class);

        final EmptyEvent1 regularEvent = new EmptyEvent1();
        final EmptyEvent2 otherEvent = new EmptyEvent2();

        final RequestEventHandler<Event> handler = context.mock(RequestEventHandler.class, "handler");
        final EventPredicate<Event> predicate = context.mock(EventPredicate.class, "predicate");

        final RequestHandleInterceptor interceptor = context.mock(RequestHandleInterceptor.class);
        extension.getRequestHandleChannel().addInterceptor(new DummyRequestHandleInterceptor(interceptor), 1);

        final AtomicReference<LowLevelHandler> lowLevelHandler = new AtomicReference<>();

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
            oneOf(interceptor).handleRequest(with(any(ChannelInvocation.class)), with(regularEvent), with(source), with(handler)); inSequence(handleChain);
            oneOf(handler).handle(with(regularEvent), with(any(ReturnEventSender.class))); inSequence(handleChain);
            // Other event
            // Expect the unwanted event to be invoked since the predicate is not tested by the StandardHandlerModule
            // In fact, the predicate is tested by the LowLevelHandlerModule
            oneOf(interceptor).handleRequest(with(any(ChannelInvocation.class)), with(otherEvent), with(source), with(handler)); inSequence(handleChain);
            oneOf(handler).handle(with(otherEvent), with(any(ReturnEventSender.class))); inSequence(handleChain);

        }});
        // @formatter:on

        extension.addRequestHandler(handler, predicate);

        lowLevelHandler.get().handle(regularEvent, source);
        lowLevelHandler.get().handle(otherEvent, source);
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testCallRequestHandlerWrongTypeInPredicate() {

        final EventPredicate<Event> predicate = context.mock(EventPredicate.class);
        final RequestEventHandler<CallableEvent> handler = new RequestEventHandler<CallableEvent>() {

            @Override
            public void handle(CallableEvent request, ReturnEventSender sender) {

                // Provoke a ClassCastException
                request.call();
            }

        };

        final AtomicReference<LowLevelHandler> lowLevelHandler = new AtomicReference<>();

        // @formatter:off
        context.checking(new Expectations() {{

            oneOf(lowLevelHandlerModule).addHandler(with(aLowLevelHandlerWithThePredicate(predicate)));
                will(storeArgument(0).in(lowLevelHandler));

        }});
        // @formatter:on

        extension.addRequestHandler(handler, predicate);

        lowLevelHandler.get().handle(new EmptyEvent1(), null);
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testCallRequestHandlerReturnEventSenders() throws BridgeConnectorException {

        final BridgeConnector source1 = null;
        final BridgeConnector source2 = context.mock(BridgeConnector.class, "source2");
        final BridgeConnector source3 = context.mock(BridgeConnector.class, "source3");

        final EmptyEvent1 returnEvent1 = new EmptyEvent1();
        final EmptyEvent2 returnEvent2 = new EmptyEvent2();
        final EmptyEvent3 returnEvent3 = new EmptyEvent3();

        final RequestEventHandler<Event> handler = context.mock(RequestEventHandler.class, "handler");
        final EventPredicate<Event> predicate = context.mock(EventPredicate.class, "predicate");

        final AtomicReference<LowLevelHandler> lowLevelHandler = new AtomicReference<>();
        final AtomicReference<ReturnEventSender> returnEventSender1 = new AtomicReference<>();
        final AtomicReference<ReturnEventSender> returnEventSender2 = new AtomicReference<>();
        final AtomicReference<ReturnEventSender> returnEventSender3 = new AtomicReference<>();

        // @formatter:off
        context.checking(new Expectations() {{

            oneOf(lowLevelHandlerModule).addHandler(with(aLowLevelHandlerWithThePredicate(predicate)));
                will(storeArgument(0).in(lowLevelHandler));

            final Sequence handlerCalls = context.sequence("handlerCalls");
            oneOf(handler).handle(with(any(EmptyEvent1.class)), with(any(ReturnEventSender.class))); inSequence(handlerCalls);
                will(storeArgument(1).in(returnEventSender1));
            oneOf(handler).handle(with(any(EmptyEvent2.class)), with(any(ReturnEventSender.class))); inSequence(handlerCalls);
                will(storeArgument(1).in(returnEventSender2));
            oneOf(handler).handle(with(any(EmptyEvent3.class)), with(any(ReturnEventSender.class))); inSequence(handlerCalls);
                will(storeArgument(1).in(returnEventSender3));

            final Sequence returnSenderCalls = context.sequence("returnSenderCalls");
            oneOf(bridge).handle(returnEvent1, null); inSequence(returnSenderCalls);
            oneOf(source2).send(returnEvent2); inSequence(handlerCalls);
            // Test exception suppression
            oneOf(source3).send(returnEvent3); inSequence(handlerCalls);
                will(throwException(new BridgeConnectorException(source3)));

        }});
        // @formatter:on

        extension.addRequestHandler(handler, predicate);

        lowLevelHandler.get().handle(new EmptyEvent1(), source1);
        lowLevelHandler.get().handle(new EmptyEvent2(), source2);
        lowLevelHandler.get().handle(new EmptyEvent3(), source3);

        returnEventSender1.get().send(returnEvent1);
        returnEventSender2.get().send(returnEvent2);
        returnEventSender3.get().send(returnEvent3);
    }

    private static class DummyRequestHandleInterceptor implements RequestHandleInterceptor {

        private final RequestHandleInterceptor dummy;

        private DummyRequestHandleInterceptor(RequestHandleInterceptor dummy) {

            this.dummy = dummy;
        }

        @Override
        public void handleRequest(ChannelInvocation<RequestHandleInterceptor> invocation, Event event, BridgeConnector source, RequestEventHandler<?> requestHandler) {

            dummy.handleRequest(invocation, event, source, requestHandler);
            invocation.next().handleRequest(invocation, event, source, requestHandler);
        }

    }

}
