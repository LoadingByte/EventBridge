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

package com.quartercode.eventbridge.channel;

import com.quartercode.eventbridge.def.channel.DefaultChannel;

/**
 * A channel invocation represents one call of a channel.
 * It is used to transport that call through all interceptors.<br>
 * See {@link DefaultChannel} and {@link DefaultChannel#invoke()} for more details on how to use interceptors.
 * 
 * @param <T> The type of interceptor that is called by the channel invocation.
 * @see DefaultChannel
 */
public interface ChannelInvocation<T> {

    /**
     * Returns the next interceptor that should be called.
     * After retrieving the next object with this method, the returned interceptor should be invoked.
     * 
     * @return The next interceptor for invocation.
     *         It should be called after retrieval.
     */
    public T next();

}
