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

import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.BridgeConnectorException;
import com.quartercode.eventbridge.bridge.module.LocalHandlerSenderModule.LocalHandlerSendInterceptor;
import com.quartercode.eventbridge.bridge.module.SenderModule;
import com.quartercode.eventbridge.bridge.module.SenderModule.SendInterceptor;
import com.quartercode.eventbridge.channel.Channel;
import com.quartercode.eventbridge.channel.ChannelInvocation;
import com.quartercode.eventbridge.def.bridge.module.DefaultLocalHandlerSenderModule;
import com.quartercode.eventbridge.test.DummyEvents.EmptyEvent1;
import com.quartercode.eventbridge.test.DummyInterceptors.DummyLocalHandlerSendInterceptor;

public class DefaultLocalHandlerSenderModuleTest {

    @Rule
    public JUnitRuleMockery                 context = new JUnitRuleMockery();

    @Mock
    private Bridge                          bridge;
    @Mock
    private SenderModule                    senderModule;
    @Mock
    private Channel<SendInterceptor>        senderModuleChannel;

    private DefaultLocalHandlerSenderModule module;

    @Before
    public void setUp() {

        module = new DefaultLocalHandlerSenderModule();

        // @formatter:off
        context.checking(new Expectations() {{

            allowing(bridge).getModule(SenderModule.class);
                will(returnValue(senderModule));
            allowing(senderModule).getChannel();
                will(returnValue(senderModuleChannel));

            // The module should add a hook to the sender module's channel
            oneOf(senderModuleChannel).addInterceptor(with(any(SendInterceptor.class)), with(100));

        }});
        // @formatter:on

        module.add(bridge);
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testSend() throws BridgeConnectorException {

        final EmptyEvent1 event = new EmptyEvent1();

        final LocalHandlerSendInterceptor interceptor = context.mock(LocalHandlerSendInterceptor.class);
        module.getChannel().addInterceptor(new DummyLocalHandlerSendInterceptor(interceptor), 1);

        // @formatter:off
        context.checking(new Expectations() {{

            final Sequence sendChain = context.sequence("sendChain");
            oneOf(interceptor).send(with(any(ChannelInvocation.class)), with(event)); inSequence(sendChain);
            oneOf(bridge).handle(null, event); inSequence(sendChain);

        }});
        // @formatter:on

        module.send(event);
    }

    @Test
    public void testRemove() throws BridgeConnectorException {

        // @formatter:off
        context.checking(new Expectations() {{

            // The module should remove its hook from the sender module's channel
            oneOf(senderModuleChannel).removeInterceptor(with(any(SendInterceptor.class)));

        }});
        // @formatter:on

        module.remove();
    }

}
