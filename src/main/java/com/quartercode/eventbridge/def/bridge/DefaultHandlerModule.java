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
import com.quartercode.eventbridge.basic.EventUtils;
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

    private final Channel<GlobalHandleInterceptor>  globalHandleChannel  = new DefaultChannel<>(GlobalHandleInterceptor.class);
    private final Channel<HandlerHandleInterceptor> handlerHandleChannel = new DefaultChannel<>(HandlerHandleInterceptor.class);

    /**
     * Creates a new default sender module.
     * 
     * @param parent The parent {@link Bridge} that uses the sender module.
     */
    public DefaultHandlerModule(Bridge parent) {

        super(parent);

        globalHandleChannel.addInterceptor(new FinalGlobalHandleInterceptor(), 0);
        handlerHandleChannel.addInterceptor(new FinalHandlerHandleInterceptor(), 0);
    }

    @Override
    public Channel<GlobalHandleInterceptor> getGlobalHandleChannel() {

        return globalHandleChannel;
    }

    @Override
    public Channel<HandlerHandleInterceptor> getHandlerHandleChannel() {

        return handlerHandleChannel;
    }

    @Override
    public void handle(Event event) {

        ChannelInvocation<GlobalHandleInterceptor> invocation = globalHandleChannel.invoke();
        invocation.next().handle(invocation, event);
    }

    private class FinalGlobalHandleInterceptor implements GlobalHandleInterceptor {

        @Override
        public void handle(ChannelInvocation<GlobalHandleInterceptor> invocation, Event event) {

            for (Pair<EventHandler<?>, EventPredicate<?>> handler : getParent().getHandlers()) {
                if (EventUtils.tryTest(handler.getRight(), event)) {
                    invokeHandlerHandleChannel(handler.getLeft(), event);
                }
            }

            invocation.next().handle(invocation, event);
        }

        private void invokeHandlerHandleChannel(EventHandler<?> handler, Event event) {

            ChannelInvocation<HandlerHandleInterceptor> newInvocation = handlerHandleChannel.invoke();
            newInvocation.next().handle(newInvocation, handler, event);
        }

    }

    /*
     * This class is static because it doesn't need a reference to DefaultHandlerModule.
     * -> Better performance
     */
    private static class FinalHandlerHandleInterceptor implements HandlerHandleInterceptor {

        @Override
        public void handle(ChannelInvocation<HandlerHandleInterceptor> invocation, EventHandler<?> handler, Event event) {

            EventUtils.tryHandle(handler, event);

            invocation.next().handle(invocation, handler, event);

        }

    }

}
