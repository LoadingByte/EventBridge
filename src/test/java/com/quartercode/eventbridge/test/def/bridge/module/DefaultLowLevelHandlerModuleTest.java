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

package com.quartercode.eventbridge.test.def.bridge.module;

import static com.quartercode.eventbridge.test.ExtraActions.storeArgument;
import static com.quartercode.eventbridge.test.ExtraAssert.assertListEquals;
import static org.junit.Assert.assertTrue;
import java.util.concurrent.atomic.AtomicReference;
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
import com.quartercode.eventbridge.bridge.module.HandlerModule;
import com.quartercode.eventbridge.bridge.module.HandlerModule.HandleInterceptor;
import com.quartercode.eventbridge.bridge.module.LowLevelHandler;
import com.quartercode.eventbridge.bridge.module.LowLevelHandlerModule.GlobalLowLevelHandleInterceptor;
import com.quartercode.eventbridge.bridge.module.LowLevelHandlerModule.ModifyLowLevelHandlerListListener;
import com.quartercode.eventbridge.bridge.module.LowLevelHandlerModule.SpecificLowLevelHandleInterceptor;
import com.quartercode.eventbridge.channel.Channel;
import com.quartercode.eventbridge.channel.ChannelInvocation;
import com.quartercode.eventbridge.def.bridge.module.DefaultLowLevelHandlerModule;
import com.quartercode.eventbridge.def.channel.DefaultChannel;
import com.quartercode.eventbridge.test.DummyEvents.CallableEvent;
import com.quartercode.eventbridge.test.DummyEvents.EmptyEvent1;
import com.quartercode.eventbridge.test.DummyEvents.EmptyEvent2;
import com.quartercode.eventbridge.test.DummyInterceptors.DummyGlobalLowLevelHandleInterceptor;
import com.quartercode.eventbridge.test.DummyInterceptors.DummySpecificLowLevelHandleInterceptor;

public class DefaultLowLevelHandlerModuleTest {

    @Rule
    public JUnitRuleMockery                          context         = new JUnitRuleMockery();

    @Mock
    private Bridge                                   bridge;
    @Mock
    private HandlerModule                            handlerModule;
    @Mock
    private Channel<HandleInterceptor>               handlerModuleChannel;

    private DefaultLowLevelHandlerModule             module;
    private final AtomicReference<HandleInterceptor> hookInterceptor = new AtomicReference<>();

    @Before
    public void setUp() {

        module = new DefaultLowLevelHandlerModule();

        // @formatter:off
        context.checking(new Expectations() {{

            allowing(bridge).getModule(HandlerModule.class);
                will(returnValue(handlerModule));
            allowing(handlerModule).getChannel();
                will(returnValue(handlerModuleChannel));

            // The module should add a hook to the handler module's channel
            oneOf(handlerModuleChannel).addInterceptor(with(any(HandleInterceptor.class)), with(100));
                will(storeArgument(0).in(hookInterceptor));

        }});
        // @formatter:on

        module.add(bridge);
    }

    @Test
    public void testRemove() throws BridgeConnectorException {

        // @formatter:off
        context.checking(new Expectations() {{

            // The module should remove its hook from the handler module's channel
            oneOf(handlerModuleChannel).removeInterceptor(with(any(HandleInterceptor.class)));

        }});
        // @formatter:on

        module.remove();
    }

    @Test
    public void testHandlerStorage() {

        final LowLevelHandler handler1 = context.mock(LowLevelHandler.class, "handler1");
        final LowLevelHandler handler2 = context.mock(LowLevelHandler.class, "handler2");
        final LowLevelHandler handler3 = context.mock(LowLevelHandler.class, "handler3");

        assertHandlerListEmpty();

        module.removeHandler(handler1);
        assertHandlerListEmpty();

        module.addHandler(handler1);
        assertListEquals("Handlers that are stored inside the module are not correct", module.getHandlers(), handler1);
        assertListEquals("Handlers that are stored inside the module changed on the second retrieval", module.getHandlers(), handler1);

        module.addHandler(handler2);
        assertListEquals("Handlers that are stored inside the module are not correct", module.getHandlers(), handler1, handler2);
        assertListEquals("Handlers that are stored inside the module changed on the second retrieval", module.getHandlers(), handler1, handler2);

        module.addHandler(handler3);
        assertListEquals("Handlers that are stored inside the module are not correct", module.getHandlers(), handler1, handler2, handler3);
        assertListEquals("Handlers that are stored inside the module changed on the second retrieval", module.getHandlers(), handler1, handler2, handler3);

        module.removeHandler(handler2);
        assertListEquals("Handlers that are stored inside the module are not correct", module.getHandlers(), handler1, handler3);
        assertListEquals("Handlers that are stored inside the module changed on the second retrieval", module.getHandlers(), handler1, handler3);
    }

    private void assertHandlerListEmpty() {

        assertTrue("There are handlers stored inside the module although none were added", module.getHandlers().isEmpty());
        assertTrue("Handlers that are stored inside the module changed on the second retrieval", module.getHandlers().isEmpty());
    }

    @Test
    public void testHandlerStorageListeners() {

        final LowLevelHandler handler = context.mock(LowLevelHandler.class, "handler");
        final ModifyLowLevelHandlerListListener listener = context.mock(ModifyLowLevelHandlerListListener.class);

        // @formatter:off
        context.checking(new Expectations() {{

            final Sequence listenerCalls = context.sequence("listenerCalls");
            oneOf(listener).onAddHandler(handler, module); inSequence(listenerCalls);
            oneOf(listener).onRemoveHandler(handler, module); inSequence(listenerCalls);

        }});
        // @formatter:on

        // Calls with listener
        module.addModifyHandlerListListener(listener);
        module.addHandler(handler);
        module.removeHandler(handler);

        // Calls without listener
        module.removeModifyHandlerListListener(listener);
        module.addHandler(handler);
        module.removeHandler(handler);
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testHandle() {

        final BridgeConnector source = context.mock(BridgeConnector.class);

        final EmptyEvent1 regularEvent = new EmptyEvent1();
        final EmptyEvent2 otherEvent = new EmptyEvent2();

        final GlobalLowLevelHandleInterceptor globalInterceptor = context.mock(GlobalLowLevelHandleInterceptor.class);
        module.getGlobalChannel().addInterceptor(new DummyGlobalLowLevelHandleInterceptor(globalInterceptor), 1);

        final SpecificLowLevelHandleInterceptor handlerInterceptor = context.mock(SpecificLowLevelHandleInterceptor.class);
        module.getSpecificChannel().addInterceptor(new DummySpecificLowLevelHandleInterceptor(handlerInterceptor), 1);

        final LowLevelHandler handler = context.mock(LowLevelHandler.class);
        final EventPredicate<Event> predicate = context.mock(EventPredicate.class);
        module.addHandler(handler);

        // @formatter:off
        context.checking(new Expectations() {{

            allowing(handler).getPredicate();
                will(returnValue(predicate));

            allowing(predicate).test(regularEvent);
                will(returnValue(true));
            allowing(predicate).test(otherEvent);
                will(returnValue(false));

            final Sequence handleChain = context.sequence("handleChain");
            // Correct event
            oneOf(globalInterceptor).handle(with(any(ChannelInvocation.class)), with(regularEvent), with(source)); inSequence(handleChain);
            oneOf(handlerInterceptor).handle(with(any(ChannelInvocation.class)), with(regularEvent), with(source), with(handler));
            oneOf(handler).handle(regularEvent, source); inSequence(handleChain);
            // Other event
            oneOf(globalInterceptor).handle(with(any(ChannelInvocation.class)), with(otherEvent), with(source)); inSequence(handleChain);

        }});
        // @formatter:on

        module.handle(regularEvent, source);

        // Test with wrongly typed event
        module.handle(otherEvent, source);
    }

    @Test
    public void testHandleWrongTypeInPredicate() {

        final LowLevelHandler handler = context.mock(LowLevelHandler.class);
        @SuppressWarnings ("serial")
        final EventPredicate<CallableEvent> predicate = new EventPredicate<CallableEvent>() {

            @Override
            public boolean test(CallableEvent event) {

                // Provoke a ClassCastException
                event.call();
                return true;
            }

        };

        // @formatter:off
        context.checking(new Expectations() {{

            allowing(handler).getPredicate();
                will(returnValue(predicate));

            never(handler).handle(with(any(Event.class)), with(any(BridgeConnector.class)));

        }});
        // @formatter:on

        module.addHandler(handler);

        // Expect the HandlerModule to suppress the resulting ClassCastException
        module.handle(new EmptyEvent1(), null);
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testHandlerModuleHook() {

        final BridgeConnector source = context.mock(BridgeConnector.class);
        final EmptyEvent1 event = new EmptyEvent1();

        // Create a dummy channel for the hook interceptor
        Channel<HandleInterceptor> dummyChannel = new DefaultChannel<>(HandleInterceptor.class);
        dummyChannel.addInterceptor(hookInterceptor.get(), 0);

        // Don't use a dummy wrapper in order to STOP the channel invocation
        // We don't want to trigger anything else in the module
        final GlobalLowLevelHandleInterceptor globalInterceptor = context.mock(GlobalLowLevelHandleInterceptor.class);
        module.getGlobalChannel().addInterceptor(globalInterceptor, 1000);

        // @formatter:off
        context.checking(new Expectations() {{

            // Expect the global channel to be invoked by the hook
            oneOf(globalInterceptor).handle(with(any(ChannelInvocation.class)), with(event), with(source));

        }});
        // @formatter:on

        // Invoke the hook
        ChannelInvocation<HandleInterceptor> dummyChannelInvocation = dummyChannel.invoke();
        dummyChannelInvocation.next().handle(dummyChannelInvocation, event, source);
    }

}
