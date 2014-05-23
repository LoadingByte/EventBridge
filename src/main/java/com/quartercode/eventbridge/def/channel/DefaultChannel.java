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

import java.util.Comparator;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.commons.lang3.Validate;
import com.quartercode.eventbridge.channel.Channel;
import com.quartercode.eventbridge.channel.ChannelInvocation;

/**
 * The default implementation of the {@link Channel} interface.
 * 
 * @param <T> The type of interceptor that can be used by the channel.
 * @see Channel
 */
public class DefaultChannel<T> implements Channel<T> {

    private Class<T>              interceptorType;
    private SortedMap<Integer, T> interceptors;

    /**
     * Creates a new default channel.
     * 
     * @param interceptorType The type of interceptor that can be used by the channel.
     */
    public DefaultChannel(Class<T> interceptorType) {

        this.interceptorType = interceptorType;

        interceptors = new TreeMap<>(new Comparator<Integer>() {

            @Override
            public int compare(Integer o1, Integer o2) {

                // Reverse the comparison in order to make the map ordering being large to small
                return Integer.compare(o2, o1);
            }

        });
    }

    @Override
    public void addInterceptor(T interceptor, int priority) {

        Validate.isTrue(!interceptors.containsKey(priority), "Event channel already contains interceptor with priority %s", priority);
        interceptors.put(priority, interceptor);
    }

    @Override
    public void removeInterceptor(T interceptor) {

        Set<Integer> removeKeys = new HashSet<>();
        for (Entry<Integer, T> storedInterceptor : interceptors.entrySet()) {
            if (storedInterceptor.getValue().equals(interceptor)) {
                removeKeys.add(storedInterceptor.getKey());
            }
        }

        for (int removeKey : removeKeys) {
            interceptors.remove(removeKey);
        }
    }

    @Override
    public ChannelInvocation<T> invoke() {

        return new DefaultChannelInvocation<>(interceptorType, interceptors.values());
    }

}
