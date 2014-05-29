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

import java.util.Arrays;
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
import com.quartercode.eventbridge.bridge.EventHandler;
import com.quartercode.eventbridge.bridge.EventPredicate;
import com.quartercode.eventbridge.bridge.HandlerModule;
import com.quartercode.eventbridge.bridge.HandlerModule.GlobalHandleInterceptor;
import com.quartercode.eventbridge.bridge.HandlerModule.HandlerHandleInterceptor;
import com.quartercode.eventbridge.channel.ChannelInvocation;
import com.quartercode.eventbridge.def.bridge.DefaultHandlerModule;
import com.quartercode.eventbridge.test.DummyEvents.CallableEvent;
import com.quartercode.eventbridge.test.DummyEvents.EmptyEvent1;
import com.quartercode.eventbridge.test.DummyEvents.EmptyEvent2;
import com.quartercode.eventbridge.test.def.bridge.DummyInterceptors.DummyGlobalHandleInterceptor;
import com.quartercode.eventbridge.test.def.bridge.DummyInterceptors.DummyHandlerHandleInterceptor;

public class DefaultHandlerModuleTest {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    @Mock
    private Bridge          bridge;
    private HandlerModule   handlerModule;

    @Before
    public void setUp() {

        handlerModule = new DefaultHandlerModule(bridge);
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

        // @formatter:off
        context.checking(new Expectations() {{

            allowing(bridge).getHandlers();
                will(returnValue(Arrays.asList(Pair.of(handler, predicate))));

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

        final EventHandler<EmptyEvent1> handler = context.mock(EventHandler.class);

        // @formatter:off
        context.checking(new Expectations() {{

            allowing(bridge).getHandlers();
                will(returnValue(Arrays.asList(Pair.of(handler, predicate))));

        }});
        // @formatter:on

        // Expect the HandlerModule to suppress the resulting ClassCastException
        handlerModule.handle(null, new EmptyEvent1());
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testHandleWrongTypeInHandler() {

        final EventHandler<CallableEvent> handler = new EventHandler<CallableEvent>() {

            @Override
            public void handle(CallableEvent event) {

                // Provoke a ClassCastException
                event.call();
            }

        };

        final EventPredicate<Event> predicate = context.mock(EventPredicate.class);

        // @formatter:off
        context.checking(new Expectations() {{

            allowing(bridge).getHandlers();
                will(returnValue(Arrays.asList(Pair.of(handler, predicate))));

            allowing(predicate).test(with(any(Event.class)));
                will(returnValue(true));

        }});
        // @formatter:on

        // Expect the HandlerModule to suppress the resulting ClassCastException
        handlerModule.handle(null, new EmptyEvent1());
    }

}
