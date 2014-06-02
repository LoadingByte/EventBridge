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
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;
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
import com.quartercode.eventbridge.bridge.module.EventHandler;
import com.quartercode.eventbridge.bridge.module.HandlerModule;
import com.quartercode.eventbridge.bridge.module.HandlerModule.HandleInterceptor;
import com.quartercode.eventbridge.channel.Channel;
import com.quartercode.eventbridge.channel.ChannelInvocation;
import com.quartercode.eventbridge.def.channel.DefaultChannel;
import com.quartercode.eventbridge.def.extra.extension.DefaultReturnEventExtensionRequester;
import com.quartercode.eventbridge.def.extra.extension.ReturnEventExtensionWrapper;
import com.quartercode.eventbridge.extra.extension.ReturnEventExtensionRequester.RequestSendInterceptor;
import com.quartercode.eventbridge.extra.extension.ReturnEventExtensionRequester.ReturnHandleInterceptor;
import com.quartercode.eventbridge.test.DummyEvents.EmptyEvent1;
import com.quartercode.eventbridge.test.DummyEvents.EmptyEvent2;

public class DefaultReturnEventExtensionRequesterTest {

    @Rule
    public JUnitRuleMockery                          context         = new JUnitRuleMockery();

    @Mock
    private Bridge                                   bridge;
    @Mock
    private HandlerModule                            handlerModule;
    @Mock
    private Channel<HandleInterceptor>               handlerModuleChannel;

    private DefaultReturnEventExtensionRequester     extension;
    private final AtomicReference<HandleInterceptor> hookInterceptor = new AtomicReference<>();

    @Before
    public void setUp() {

        extension = new DefaultReturnEventExtensionRequester();

        // @formatter:off
        context.checking(new Expectations() {{

            allowing(bridge).getModule(HandlerModule.class);
                will(returnValue(handlerModule));
            allowing(handlerModule).getChannel();
                will(returnValue(handlerModuleChannel));

            // The module should add a hook to the handler module's channel
            allowing(handlerModuleChannel).addInterceptor(with(any(HandleInterceptor.class)), with(500));
                will(storeArgument(0).in(hookInterceptor));

        }});
        // @formatter:on

        extension.add(bridge);
    }

    @Test
    public void testRemove() throws BridgeConnectorException {

        // @formatter:off
        context.checking(new Expectations() {{

            // The module should remove its hook from the handler module's channel
            oneOf(handlerModuleChannel).removeInterceptor(with(any(HandleInterceptor.class)));

        }});
        // @formatter:on

        extension.remove();
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testSendRequest() {

        final EmptyEvent1 requestEvent = new EmptyEvent1();
        final EventHandler<?> returnHandler = context.mock(EventHandler.class);

        final RequestSendInterceptor requestSendInterceptor = context.mock(RequestSendInterceptor.class);
        extension.getRequestSendChannel().addInterceptor(new DummyRequestSendInterceptor(requestSendInterceptor), 1);

        final AtomicReference<ReturnEventExtensionWrapper> requestWrapper = new AtomicReference<>();

        // @formatter:off
        context.checking(new Expectations() {{

            final Sequence sendChain = context.sequence("sendChain");
            oneOf(requestSendInterceptor).sendRequest(with(any(ChannelInvocation.class)), with(requestEvent), with(returnHandler)); inSequence(sendChain);
            oneOf(bridge).send(with(any(ReturnEventExtensionWrapper.class)));
                will(storeArgument(0).in(requestWrapper));

        }});
        // @formatter:on

        extension.sendRequest(requestEvent, returnHandler);

        assertNotNull("Extension didn't send wrapper request event", requestWrapper.get());
        assertEquals("Wrapper request event doesn't contain correct request event", requestEvent, requestWrapper.get().getEvent());
        assertTrue("Wrapper request event isn't an actual request", requestWrapper.get().isRequest());
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testHandlReturnHandleChannel() {

        final EmptyEvent2 returnEvent = new EmptyEvent2();
        final EventHandler<EmptyEvent2> returnHandler = context.mock(EventHandler.class);

        // @formatter:off
        context.checking(new Expectations() {{

            // Expect the return handler to be invoked
            oneOf(returnHandler).handle(returnEvent);

        }});
        // @formatter:on

        ChannelInvocation<ReturnHandleInterceptor> invocation = extension.getReturnHandleChannel().invoke();
        invocation.next().handleReturn(invocation, returnEvent, null, returnHandler);
    }

    private ReturnEventExtensionWrapper sendAndCatchRequest(Event requestEvent, EventHandler<?> returnHandler) {

        final AtomicReference<ReturnEventExtensionWrapper> requestWrapper = new AtomicReference<>();

        // @formatter:off
        context.checking(new Expectations() {{

            oneOf(bridge).send(with(any(ReturnEventExtensionWrapper.class)));
                will(storeArgument(0).in(requestWrapper));

        }});
        // @formatter:on

        extension.sendRequest(requestEvent, returnHandler);

        return requestWrapper.get();
    }

    private void invokeHook(ReturnEventExtensionWrapper returnWrapper) {

        // Create a dummy channel for the hook interceptor
        Channel<HandleInterceptor> dummyChannel = new DefaultChannel<>(HandleInterceptor.class);
        dummyChannel.addInterceptor(hookInterceptor.get(), 0);

        // Invoke the hook
        ChannelInvocation<HandleInterceptor> dummyChannelInvocation = dummyChannel.invoke();
        dummyChannelInvocation.next().handle(dummyChannelInvocation, returnWrapper, null);
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testHandlerModuleHook() {

        final EmptyEvent2 returnEvent = new EmptyEvent2();
        final EventHandler<EmptyEvent2> returnHandler = context.mock(EventHandler.class);

        // Send request
        ReturnEventExtensionWrapper requestWrapper = sendAndCatchRequest(new EmptyEvent1(), returnHandler);

        // Don't use a dummy wrapper in order to STOP the channel invocation
        // We don't want to trigger anything else in the module
        final ReturnHandleInterceptor returnHandleInterceptor = context.mock(ReturnHandleInterceptor.class);
        extension.getReturnHandleChannel().addInterceptor(returnHandleInterceptor, 1);

        // @formatter:off
        context.checking(new Expectations() {{

            // Expect the return handler to be invoked
            oneOf(returnHandleInterceptor).handleReturn(with(any(ChannelInvocation.class)), with(returnEvent), with(nullValue(BridgeConnector.class)), with(returnHandler));

        }});
        // @formatter:on

        ReturnEventExtensionWrapper returnWrapper = new ReturnEventExtensionWrapper(returnEvent, requestWrapper.getRequestId(), false);
        invokeHook(returnWrapper);
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testHandlerModuleHookAndReturnHandling() {

        final EmptyEvent2 returnEvent = new EmptyEvent2();
        final EventHandler<EmptyEvent2> returnHandler = context.mock(EventHandler.class);

        // Send request
        ReturnEventExtensionWrapper requestWrapper = sendAndCatchRequest(new EmptyEvent1(), returnHandler);

        // @formatter:off
        context.checking(new Expectations() {{

            // Expect the return handler to be invoked
            oneOf(returnHandler).handle(returnEvent);

        }});
        // @formatter:on

        invokeHook(new ReturnEventExtensionWrapper(returnEvent, requestWrapper.getRequestId(), false));
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testHandlerModuleHookWithUnknownRequestId() {

        final EmptyEvent2 returnEvent = new EmptyEvent2();
        final EventHandler<EmptyEvent2> returnHandler = context.mock(EventHandler.class);

        // Send request
        sendAndCatchRequest(new EmptyEvent1(), returnHandler);

        // @formatter:off
        context.checking(new Expectations() {{

            // Expect the return handler not to be invoked
            never(returnHandler).handle(returnEvent);

        }});
        // @formatter:on

        // -1 is an impossible id since the counter begins with 0
        invokeHook(new ReturnEventExtensionWrapper(returnEvent, -1, false));
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testNonReturnWrapperAndOtherEventsPassHandleChannel() {

        final ReturnEventExtensionWrapper nonReturnWrapper = new ReturnEventExtensionWrapper(new EmptyEvent1(), 0, true);
        final EmptyEvent2 otherEvent = new EmptyEvent2();

        final HandleInterceptor lastInterceptor = context.mock(HandleInterceptor.class);

        // @formatter:off
        context.checking(new Expectations() {{

            oneOf(lastInterceptor).handle(with(any(ChannelInvocation.class)), with(nonReturnWrapper), with(nullValue(BridgeConnector.class)));
            oneOf(lastInterceptor).handle(with(any(ChannelInvocation.class)), with(otherEvent), with(nullValue(BridgeConnector.class)));

        }});
        // @formatter:on

        // Create a dummy channel for the hook interceptor
        Channel<HandleInterceptor> dummyChannel = new DefaultChannel<>(HandleInterceptor.class);
        dummyChannel.addInterceptor(hookInterceptor.get(), 1);
        dummyChannel.addInterceptor(lastInterceptor, 0);

        // Invoke the hook
        ChannelInvocation<HandleInterceptor> dummyChannelInvocation1 = dummyChannel.invoke();
        dummyChannelInvocation1.next().handle(dummyChannelInvocation1, nonReturnWrapper, null);

        ChannelInvocation<HandleInterceptor> dummyChannelInvocation2 = dummyChannel.invoke();
        dummyChannelInvocation2.next().handle(dummyChannelInvocation2, otherEvent, null);
    }

    private static class DummyRequestSendInterceptor implements RequestSendInterceptor {

        private final RequestSendInterceptor dummy;

        private DummyRequestSendInterceptor(RequestSendInterceptor dummy) {

            this.dummy = dummy;
        }

        @Override
        public void sendRequest(ChannelInvocation<RequestSendInterceptor> invocation, Event request, EventHandler<?> returnHandler) {

            dummy.sendRequest(invocation, request, returnHandler);
            invocation.next().sendRequest(invocation, request, returnHandler);
        }

    }

}
