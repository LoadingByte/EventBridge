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

package com.quartercode.eventbridge.def.bridge;

import org.apache.commons.lang3.tuple.Pair;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.EventHandler;
import com.quartercode.eventbridge.bridge.EventPredicate;
import com.quartercode.eventbridge.bridge.HandlerModule;
import com.quartercode.eventbridge.bridge.SenderModule;
import com.quartercode.eventbridge.channel.Channel;
import com.quartercode.eventbridge.channel.ChannelInvocation;
import com.quartercode.eventbridge.def.channel.DefaultChannel;

/**
 * The default default implementation of the {@link DefaultHandlerModule} interface.
 * 
 * @see SenderModule
 */
public class DefaultHandlerModule extends BridgeModuleBase implements HandlerModule {

    private final Channel<HandleInterceptor> handleChannel = new DefaultChannel<>(HandleInterceptor.class);

    /**
     * Creates a new default sender module.
     * 
     * @param parent The parent {@link Bridge} that uses the sender module.
     */
    public DefaultHandlerModule(Bridge parent) {

        super(parent);

        handleChannel.addInterceptor(new HandleInterceptor() {

            @Override
            public void handle(ChannelInvocation<HandleInterceptor> invocation, Event event) {

                for (Pair<EventHandler<?>, EventPredicate<?>> handler : getParent().getHandlers()) {
                    if (EventUtils.tryTest(handler.getRight(), event)) {
                        EventUtils.tryHandle(handler.getLeft(), event);
                    }
                }

                invocation.next().handle(invocation, event);
            }

        }, 0);
    }

    @Override
    public Channel<HandleInterceptor> getHandleChannel() {

        return handleChannel;
    }

    @Override
    public void handle(Event event) {

        ChannelInvocation<HandleInterceptor> invocation = handleChannel.invoke();
        invocation.next().handle(invocation, event);
    }

}