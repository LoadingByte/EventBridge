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

package com.quartercode.eventbridge.test.def.channel;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.quartercode.eventbridge.channel.ChannelInvocation;
import com.quartercode.eventbridge.def.channel.DefaultChannel;

public class ChannelReturnValuesTest {

    @Test
    public void testGeneratedInterceptorReturnValues() {

        // ----- Unboxed -----

        ChannelInvocation<ByteInterceptor> byteInvocation = new DefaultChannel<>(ByteInterceptor.class).invoke();
        assertTypeAndValueEquals("byte interceptor", (byte) 0, byteInvocation.next().run(byteInvocation));

        ChannelInvocation<ShortInterceptor> shortInvocation = new DefaultChannel<>(ShortInterceptor.class).invoke();
        assertTypeAndValueEquals("short interceptor", (short) 0, shortInvocation.next().run(shortInvocation));

        ChannelInvocation<IntegerInterceptor> integerInvocation = new DefaultChannel<>(IntegerInterceptor.class).invoke();
        assertTypeAndValueEquals("integer interceptor", 0, integerInvocation.next().run(integerInvocation));

        ChannelInvocation<LongInterceptor> longInvocation = new DefaultChannel<>(LongInterceptor.class).invoke();
        assertTypeAndValueEquals("long interceptor", (long) 0, longInvocation.next().run(longInvocation));

        ChannelInvocation<FloatInterceptor> floatInvocation = new DefaultChannel<>(FloatInterceptor.class).invoke();
        assertTypeAndValueEquals("float interceptor", (float) 0, floatInvocation.next().run(floatInvocation));

        ChannelInvocation<DoubleInterceptor> doubleInvocation = new DefaultChannel<>(DoubleInterceptor.class).invoke();
        assertTypeAndValueEquals("double interceptor", (double) 0, doubleInvocation.next().run(doubleInvocation));

        ChannelInvocation<BooleanInterceptor> booleanInvocation = new DefaultChannel<>(BooleanInterceptor.class).invoke();
        assertTypeAndValueEquals("boolean interceptor", false, booleanInvocation.next().run(booleanInvocation));

        ChannelInvocation<CharInterceptor> charInvocation = new DefaultChannel<>(CharInterceptor.class).invoke();
        assertTypeAndValueEquals("char interceptor", '\u0000', charInvocation.next().run(charInvocation));

        // ----- Boxed -----

        ChannelInvocation<BoxedByteInterceptor> boxedByteInvocation = new DefaultChannel<>(BoxedByteInterceptor.class).invoke();
        assertTypeAndValueEquals("boxed byte interceptor", (byte) 0, boxedByteInvocation.next().run(boxedByteInvocation));

        ChannelInvocation<BoxedShortInterceptor> boxedShortInvocation = new DefaultChannel<>(BoxedShortInterceptor.class).invoke();
        assertTypeAndValueEquals("boxed short interceptor", (short) 0, boxedShortInvocation.next().run(boxedShortInvocation));

        ChannelInvocation<BoxedIntegerInterceptor> boxedIntegerInvocation = new DefaultChannel<>(BoxedIntegerInterceptor.class).invoke();
        assertTypeAndValueEquals("boxed integer interceptor", 0, boxedIntegerInvocation.next().run(boxedIntegerInvocation));

        ChannelInvocation<BoxedLongInterceptor> boxedLongInvocation = new DefaultChannel<>(BoxedLongInterceptor.class).invoke();
        assertTypeAndValueEquals("boxed long interceptor", (long) 0, boxedLongInvocation.next().run(boxedLongInvocation));

        ChannelInvocation<BoxedFloatInterceptor> boxedFloatInvocation = new DefaultChannel<>(BoxedFloatInterceptor.class).invoke();
        assertTypeAndValueEquals("boxed float interceptor", (float) 0, boxedFloatInvocation.next().run(boxedFloatInvocation));

        ChannelInvocation<BoxedDoubleInterceptor> boxedDoubleInvocation = new DefaultChannel<>(BoxedDoubleInterceptor.class).invoke();
        assertTypeAndValueEquals("boxed double interceptor", (double) 0, boxedDoubleInvocation.next().run(boxedDoubleInvocation));

        ChannelInvocation<BoxedBooleanInterceptor> boxedBooleanInvocation = new DefaultChannel<>(BoxedBooleanInterceptor.class).invoke();
        assertTypeAndValueEquals("boxed boolean interceptor", false, boxedBooleanInvocation.next().run(boxedBooleanInvocation));

        ChannelInvocation<BoxedCharInterceptor> boxedCharInvocation = new DefaultChannel<>(BoxedCharInterceptor.class).invoke();
        assertTypeAndValueEquals("boxed char interceptor", '\u0000', boxedCharInvocation.next().run(boxedCharInvocation));

        // ----- Non-Primitive -----

        ChannelInvocation<StringInterceptor> stringInvocation = new DefaultChannel<>(StringInterceptor.class).invoke();
        assertTypeAndValueEquals("string interceptor", null, stringInvocation.next().run(stringInvocation));
    }

    private void assertTypeAndValueEquals(String generatedInterceptorName, Object expected, Object actual) {

        if (expected != null && actual != null) {
            assertEquals("Type of return value of generated " + generatedInterceptorName, expected.getClass(), actual.getClass());
        }
        assertEquals("Return value of generated " + generatedInterceptorName, expected, actual);
    }

    // ----- Unboxed -----

    private static interface ByteInterceptor {

        public byte run(ChannelInvocation<ByteInterceptor> invocation);

    }

    private static interface ShortInterceptor {

        public short run(ChannelInvocation<ShortInterceptor> invocation);

    }

    private static interface IntegerInterceptor {

        public int run(ChannelInvocation<IntegerInterceptor> invocation);

    }

    private static interface LongInterceptor {

        public long run(ChannelInvocation<LongInterceptor> invocation);

    }

    private static interface FloatInterceptor {

        public float run(ChannelInvocation<FloatInterceptor> invocation);

    }

    private static interface DoubleInterceptor {

        public double run(ChannelInvocation<DoubleInterceptor> invocation);

    }

    private static interface BooleanInterceptor {

        public boolean run(ChannelInvocation<BooleanInterceptor> invocation);

    }

    private static interface CharInterceptor {

        public char run(ChannelInvocation<CharInterceptor> invocation);

    }

    // ----- Boxed -----

    private static interface BoxedByteInterceptor {

        public Byte run(ChannelInvocation<BoxedByteInterceptor> invocation);

    }

    private static interface BoxedShortInterceptor {

        public Short run(ChannelInvocation<BoxedShortInterceptor> invocation);

    }

    private static interface BoxedIntegerInterceptor {

        public Integer run(ChannelInvocation<BoxedIntegerInterceptor> invocation);

    }

    private static interface BoxedLongInterceptor {

        public Long run(ChannelInvocation<BoxedLongInterceptor> invocation);

    }

    private static interface BoxedFloatInterceptor {

        public Float run(ChannelInvocation<BoxedFloatInterceptor> invocation);

    }

    private static interface BoxedDoubleInterceptor {

        public Double run(ChannelInvocation<BoxedDoubleInterceptor> invocation);

    }

    private static interface BoxedBooleanInterceptor {

        public Boolean run(ChannelInvocation<BoxedBooleanInterceptor> invocation);

    }

    private static interface BoxedCharInterceptor {

        public Character run(ChannelInvocation<BoxedCharInterceptor> invocation);

    }

    // ----- Non-Primitive -----

    private static interface StringInterceptor {

        public String run(ChannelInvocation<StringInterceptor> invocation);

    }

}
