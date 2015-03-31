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

package com.quartercode.eventbridge.channel;

import com.quartercode.eventbridge.def.channel.DefaultChannel;

/**
 * Channels are method pipelines which take a method call and invoke all assigned interceptors.
 * That way, method calls can be dynamically extended.<br>
 * <br>
 * Interceptor interfaces should define a {@link ChannelInvocation} argument.
 * It is used to keep a channel call running through all the interceptors.
 * An interceptor implementation might look like this:
 * 
 * <pre>
 * public class TestExampleInterceptor implements ExampleInterceptor {
 * 
 *     public void run(ChannelInvocation&lt;ExampleInterceptor&gt; invocation, int someArgument) {
 * 
 *         // Do something before the final channel call (default channel functionality)
 * 
 *         invocation.next().run(invocation, someArgument);
 * 
 *         // Do something after the final channel call (default channel functionality)
 *     }
 * 
 * } .
 * </pre>
 * 
 * @param <T> The type of interceptor that can be used by the channel.
 * @see ChannelInvocation
 */
public interface Channel<T> {

    /**
     * Adds a new interceptor to the channel.
     * Interceptors with a higher priority are invoked first.
     * One interceptor instance can be added multiple times under different priorities.<br>
     * See the {@link DefaultChannel} javadoc for more details on interceptors.
     * 
     * @param interceptor The interceptor to add to the channel.
     * @param priority The priority under which the interceptor should be added.
     *        There can be only one interceptor with a specific priority value added to a channel.
     */
    public void addInterceptor(T interceptor, int priority);

    /**
     * Removes an interceptor from the channel.
     * If the interceptor instance is added multiple times under different priorities, all references to the interceptors are removed.<br>
     * See the {@link DefaultChannel} javadoc for more details on interceptors.
     * 
     * @param interceptor The interceptor to remove from the channel.
     */
    public void removeInterceptor(T interceptor);

    /**
     * Creates a new {@link ChannelInvocation} instance which handles the invocation of all interceptors.
     * The returned invocation can be started by calling the interceptor function on the first interceptor:
     * 
     * <pre>
     * ChannelInvocation&lt;<i>XYZInterceptor</i>&gt; invocation = <i>channel</i>.invoke();
     * invocation.next().<i>xyz</i>(invocation, ...);
     * </pre>
     * 
     * @return A new channel invocation instance for the channel.
     */
    public ChannelInvocation<T> invoke();

}
