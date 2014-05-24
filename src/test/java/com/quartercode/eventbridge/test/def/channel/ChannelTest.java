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

package com.quartercode.eventbridge.test.def.channel;

import static org.junit.Assert.assertEquals;
import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import com.quartercode.eventbridge.channel.Channel;
import com.quartercode.eventbridge.channel.ChannelInvocation;
import com.quartercode.eventbridge.def.channel.DefaultChannel;

public class ChannelTest {

    @Rule
    public JUnitRuleMockery          context = new JUnitRuleMockery();

    private Channel<TestInterceptor> channel;

    @Before
    public void setUp() {

        channel = new DefaultChannel<>(TestInterceptor.class);
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testInvokeOneInterceptor() {

        final String[] testArguments = { "test1", "test2", "test3" };

        final TestInterceptor interceptor = context.mock(TestInterceptor.class);
        channel.addInterceptor(interceptor, 0);

        // @formatter:off
        context.checking(new Expectations() {{

            oneOf(interceptor).run(with(any(ChannelInvocation.class)), with(testArguments));
                will(returnValue("testResult"));

        }});
        // @formatter:on

        ChannelInvocation<TestInterceptor> invocation = channel.invoke();
        String result = invocation.next().run(invocation, testArguments);

        assertEquals("Value that was returned by the channel", "testResult", result);
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testInvokeMultipleInterceptors() {

        final String[] testArguments = { "test1", "test2", "test3" };

        final TestInterceptor interceptor2 = context.mock(TestInterceptor.class, "interceptor2");
        final TestInterceptor interceptor1 = context.mock(TestInterceptor.class, "interceptor1");
        final TestInterceptor interceptor0 = context.mock(TestInterceptor.class, "interceptor0");

        // Don't use the correct ordering in order to check the priority system
        channel.addInterceptor(interceptor1, 1);
        channel.addInterceptor(new DummyTestInterceptor(interceptor0), 0);
        channel.removeInterceptor(interceptor1);
        channel.addInterceptor(new DummyTestInterceptor(interceptor2), 2);
        channel.addInterceptor(new DummyTestInterceptor(interceptor1), 1);

        // @formatter:off
        context.checking(new Expectations() {{

            final Sequence interceptorCalls = context.sequence("interceptorCalls");

            oneOf(interceptor2).run(with(any(ChannelInvocation.class)), with(testArguments)); inSequence(interceptorCalls);
                will(returnValue("testResult"));
            oneOf(interceptor1).run(with(any(ChannelInvocation.class)), with(testArguments)); inSequence(interceptorCalls);
            oneOf(interceptor0).run(with(any(ChannelInvocation.class)), with(testArguments)); inSequence(interceptorCalls);

        }});
        // @formatter:on

        ChannelInvocation<TestInterceptor> invocation = channel.invoke();
        String result = invocation.next().run(invocation, testArguments);

        assertEquals("Value that was returned by the channel", "testResult", result);
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testInvokeInterceptorTwice() {

        final String[] testArguments = { "test1", "test2", "test3" };

        final TestInterceptor interceptor = context.mock(TestInterceptor.class);

        channel.addInterceptor(new DummyTestInterceptor(interceptor), 0);
        channel.addInterceptor(new DummyTestInterceptor(interceptor), 1);

        // @formatter:off
        context.checking(new Expectations() {{

            exactly(2).of(interceptor).run(with(any(ChannelInvocation.class)), with(testArguments));

        }});
        // @formatter:on

        ChannelInvocation<TestInterceptor> invocation = channel.invoke();
        invocation.next().run(invocation, testArguments);
    }

    @Test
    public void testInvokeMultipleInterceptorsReturnValue() {

        TestInterceptor interceptor2 = new TestInterceptor() {

            @Override
            public String run(ChannelInvocation<TestInterceptor> invocation, String[] testArguments) {

                return invocation.next().run(invocation, testArguments) + "2";
            };

        };
        TestInterceptor interceptor1 = new TestInterceptor() {

            @Override
            public String run(ChannelInvocation<TestInterceptor> invocation, String[] testArguments) {

                return invocation.next().run(invocation, testArguments) + "1";
            };

        };
        TestInterceptor interceptor0 = new TestInterceptor() {

            @Override
            public String run(ChannelInvocation<TestInterceptor> invocation, String[] testArguments) {

                invocation.next().run(invocation, testArguments);
                return "0";
            };

        };

        channel.addInterceptor(interceptor2, 2);
        channel.addInterceptor(interceptor1, 1);
        channel.addInterceptor(interceptor0, 0);

        ChannelInvocation<TestInterceptor> invocation = channel.invoke();
        String result = invocation.next().run(invocation, null);

        assertEquals("Value that was returned by the channel", "012", result);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testAddInterceptorsWithSamePriority() {

        channel.addInterceptor(context.mock(TestInterceptor.class, "interceptor1"), 0);
        channel.addInterceptor(context.mock(TestInterceptor.class, "interceptor2"), 0);
    }

}
