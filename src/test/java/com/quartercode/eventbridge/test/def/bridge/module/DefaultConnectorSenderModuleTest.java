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

import java.util.Arrays;
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
import com.quartercode.eventbridge.bridge.module.ConnectorSenderModule.GlobalConnectorSendInterceptor;
import com.quartercode.eventbridge.bridge.module.ConnectorSenderModule.SpecificConnectorSendInterceptor;
import com.quartercode.eventbridge.bridge.module.SenderModule;
import com.quartercode.eventbridge.bridge.module.SenderModule.SendInterceptor;
import com.quartercode.eventbridge.channel.Channel;
import com.quartercode.eventbridge.channel.ChannelInvocation;
import com.quartercode.eventbridge.def.bridge.module.DefaultConnectorSenderModule;
import com.quartercode.eventbridge.test.DummyEvents.EmptyEvent1;
import com.quartercode.eventbridge.test.DummyInterceptors.DummyGlobalConnectorSendInterceptor;
import com.quartercode.eventbridge.test.DummyInterceptors.DummySpecificConnectorSendInterceptor;

public class DefaultConnectorSenderModuleTest {

    @Rule
    public JUnitRuleMockery              context = new JUnitRuleMockery();

    @Mock
    private Bridge                       bridge;
    @Mock
    private SenderModule                 senderModule;
    @Mock
    private Channel<SendInterceptor>     senderModuleChannel;

    private DefaultConnectorSenderModule module;

    @Before
    public void setUp() {

        module = new DefaultConnectorSenderModule();

        // @formatter:off
        context.checking(new Expectations() {{

            allowing(bridge).getModule(SenderModule.class);
                will(returnValue(senderModule));
            allowing(senderModule).getChannel();
                will(returnValue(senderModuleChannel));

            // The module should add a hook to the sender module's channel
            oneOf(senderModuleChannel).addInterceptor(with(any(SendInterceptor.class)), with(50));

        }});
        // @formatter:on

        module.add(bridge);
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testSend() throws BridgeConnectorException {

        final EmptyEvent1 event = new EmptyEvent1();

        final GlobalConnectorSendInterceptor globalInterceptor = context.mock(GlobalConnectorSendInterceptor.class);
        module.getGlobalChannel().addInterceptor(new DummyGlobalConnectorSendInterceptor(globalInterceptor), 1);

        final SpecificConnectorSendInterceptor specificInterceptor = context.mock(SpecificConnectorSendInterceptor.class);
        module.getSpecificChannel().addInterceptor(new DummySpecificConnectorSendInterceptor(specificInterceptor), 1);

        final BridgeConnector connector1 = context.mock(BridgeConnector.class, "connector1");
        final BridgeConnector connector2 = context.mock(BridgeConnector.class, "connector2");

        // @formatter:off
        context.checking(new Expectations() {{

            allowing(bridge).getConnectors();
                will(returnValue(Arrays.asList(connector1, connector2)));

            final Sequence sendChain = context.sequence("sendChain");
            oneOf(globalInterceptor).send(with(any(ChannelInvocation.class)), with(event)); inSequence(sendChain);
            oneOf(specificInterceptor).send(with(any(ChannelInvocation.class)), with(event), with(connector1)); inSequence(sendChain);
            oneOf(connector1).send(event); inSequence(sendChain);
            oneOf(specificInterceptor).send(with(any(ChannelInvocation.class)), with(event), with(connector2)); inSequence(sendChain);
            oneOf(connector2).send(event); inSequence(sendChain);

        }});
        // @formatter:on

        module.send(event);
    }

    @Test
    public void testSendWithException() throws BridgeConnectorException {

        final EmptyEvent1 event = new EmptyEvent1();

        final BridgeConnector connector = context.mock(BridgeConnector.class);

        // @formatter:off
        context.checking(new Expectations() {{

            allowing(bridge).getConnectors();
                will(returnValue(Arrays.asList(connector)));

            oneOf(connector).send(event);
                will(throwException(new BridgeConnectorException(connector)));

        }});
        // @formatter:on

        // Assert that the exception is suppressed by the sender module
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
