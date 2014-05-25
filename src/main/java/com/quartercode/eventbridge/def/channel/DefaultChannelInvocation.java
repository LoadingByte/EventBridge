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

package com.quartercode.eventbridge.def.channel;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import com.quartercode.eventbridge.channel.ChannelInvocation;

/**
 * The default default implementation of the {@link ChannelInvocation} interface.
 * 
 * @param <T> The type of interceptor that is called by the channel invocation.
 * @see ChannelInvocation
 */
class DefaultChannelInvocation<T> implements ChannelInvocation<T> {

    private final Class<T> interceptorType;
    private final Queue<T> interceptors;

    /**
     * Creates a new default channel invocation object.
     * 
     * @param interceptorType The type of interceptor that is called by the default channel invocation.
     * @param interceptors The actual interceptors for calling in the correct order (index {@code 0} is called first).
     */
    public DefaultChannelInvocation(Class<T> interceptorType, Collection<T> interceptors) {

        this.interceptorType = interceptorType;
        this.interceptors = new LinkedList<>(interceptors);
    }

    @Override
    public T next() {

        T next = interceptors.poll();

        if (next == null) {
            next = createEmptyInterceptor();
        }

        return next;
    }

    @SuppressWarnings ("unchecked")
    private T createEmptyInterceptor() {

        return (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { interceptorType }, new EmptyInvocationHandler());
    }

    private static class EmptyInvocationHandler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            return getDefaultValue(method.getReturnType());
        }

        private Object getDefaultValue(Class<?> type) {

            if (type.equals(Byte.TYPE) || type.equals(Byte.class)) {
                return (byte) 0;
            } else if (type.equals(Short.TYPE) || type.equals(Short.class)) {
                return (short) 0;
            } else if (type.equals(Integer.TYPE) || type.equals(Integer.class)) {
                return (int) 0;
            } else if (type.equals(Long.TYPE) || type.equals(Long.class)) {
                return (long) 0;
            } else if (type.equals(Float.TYPE) || type.equals(Float.class)) {
                return (float) 0;
            } else if (type.equals(Double.TYPE) || type.equals(Double.class)) {
                return (double) 0;
            } else if (type.equals(Boolean.TYPE) || type.equals(Boolean.class)) {
                return false;
            } else if (type.equals(Character.TYPE) || type.equals(Character.class)) {
                return '\u0000';
            } else {
                return null;
            }
        }

    }

}
