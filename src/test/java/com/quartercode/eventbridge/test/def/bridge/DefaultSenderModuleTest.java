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

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.BridgeConnectorException;
import com.quartercode.eventbridge.bridge.SenderModule.SendInterceptor;
import com.quartercode.eventbridge.channel.ChannelInvocation;
import com.quartercode.eventbridge.def.bridge.DefaultSenderModule;
import com.quartercode.eventbridge.test.DummyEvents.EmptyEvent1;
import com.quartercode.eventbridge.test.DummyInterceptors.DummySendInterceptor;

public class DefaultSenderModuleTest {

    @Rule
    public JUnitRuleMockery     context = new JUnitRuleMockery();

    @Mock
    private Bridge              bridge;
    private DefaultSenderModule module;

    @Before
    public void setUp() {

        module = new DefaultSenderModule();
        module.add(bridge);
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testSend() throws BridgeConnectorException {

        final EmptyEvent1 event = new EmptyEvent1();

        final SendInterceptor interceptor = context.mock(SendInterceptor.class);
        module.getChannel().addInterceptor(new DummySendInterceptor(interceptor), 1);

        // @formatter:off
        context.checking(new Expectations() {{

            oneOf(interceptor).send(with(any(ChannelInvocation.class)), with(event));

        }});
        // @formatter:on

        module.send(event);
    }

}
