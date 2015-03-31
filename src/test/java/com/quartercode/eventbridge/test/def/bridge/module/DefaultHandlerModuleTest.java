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

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.BridgeConnectorException;
import com.quartercode.eventbridge.bridge.module.HandlerModule.HandleInterceptor;
import com.quartercode.eventbridge.channel.ChannelInvocation;
import com.quartercode.eventbridge.def.bridge.module.DefaultHandlerModule;
import com.quartercode.eventbridge.test.DummyEvents.EmptyEvent1;
import com.quartercode.eventbridge.test.DummyInterceptors.DummyHandleInterceptor;

public class DefaultHandlerModuleTest {

    @Rule
    public JUnitRuleMockery      context = new JUnitRuleMockery();

    @Mock
    private Bridge               bridge;
    private DefaultHandlerModule module;

    @Before
    public void setUp() {

        module = new DefaultHandlerModule();
        module.add(bridge);
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testSend() throws BridgeConnectorException {

        final EmptyEvent1 event = new EmptyEvent1();
        final BridgeConnector source = context.mock(BridgeConnector.class);

        final HandleInterceptor interceptor = context.mock(HandleInterceptor.class);
        module.getChannel().addInterceptor(new DummyHandleInterceptor(interceptor), 1);

        // @formatter:off
        context.checking(new Expectations() {{

            oneOf(interceptor).handle(with(any(ChannelInvocation.class)), with(event),with(source));

        }});
        // @formatter:on

        module.handle(event, source);
    }

}
