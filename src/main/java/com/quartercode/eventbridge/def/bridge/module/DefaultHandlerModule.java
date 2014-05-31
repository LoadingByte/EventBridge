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

package com.quartercode.eventbridge.def.bridge.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import com.quartercode.eventbridge.basic.AbstractBridgeModule;
import com.quartercode.eventbridge.basic.EventUtils;
import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.EventHandler;
import com.quartercode.eventbridge.bridge.EventPredicate;
import com.quartercode.eventbridge.bridge.module.HandlerModule;
import com.quartercode.eventbridge.channel.Channel;
import com.quartercode.eventbridge.channel.ChannelInvocation;
import com.quartercode.eventbridge.def.channel.DefaultChannel;

/**
 * The default default implementation of the {@link DefaultHandlerModule} interface.
 * 
 * @see HandlerModule
 */
public class DefaultHandlerModule extends AbstractBridgeModule implements HandlerModule {

    private final Channel<GlobalHandleInterceptor>        globalHandleChannel        = new DefaultChannel<>(GlobalHandleInterceptor.class);
    private final Channel<HandlerHandleInterceptor>       handlerHandleChannel       = new DefaultChannel<>(HandlerHandleInterceptor.class);

    private final Map<EventHandler<?>, EventPredicate<?>> handlers                   = new ConcurrentHashMap<>();
    private final List<ModifyHandlerListListener>         modifyHandlerListListeners = new ArrayList<>();
    private Map<EventHandler<?>, EventPredicate<?>>       handlersUnmodifiableCache;

    /**
     * Creates a new default sender module.
     */
    public DefaultHandlerModule() {

        globalHandleChannel.addInterceptor(new FinalGlobalHandleInterceptor(), 0);
        handlerHandleChannel.addInterceptor(new FinalHandlerHandleInterceptor(), 0);
    }

    @Override
    public Map<EventHandler<?>, EventPredicate<?>> getHandlers() {

        if (handlersUnmodifiableCache == null) {
            handlersUnmodifiableCache = Collections.unmodifiableMap(handlers);
        }

        return handlersUnmodifiableCache;
    }

    @Override
    public void addHandler(EventHandler<?> handler, EventPredicate<?> predicate) {

        handlers.put(handler, predicate);
        handlersUnmodifiableCache = null;

        for (ModifyHandlerListListener listener : modifyHandlerListListeners) {
            listener.onAddHandler(handler, predicate, this);
        }
    }

    @Override
    public void removeHandler(EventHandler<?> handler) {

        if (handlers.containsKey(handler)) {
            if (!modifyHandlerListListeners.isEmpty()) {
                EventPredicate<?> predicate = handlers.get(handler);
                for (ModifyHandlerListListener listener : modifyHandlerListListeners) {
                    listener.onRemoveHandler(handler, predicate, this);
                }
            }

            handlers.remove(handler);
            handlersUnmodifiableCache = null;
        }
    }

    @Override
    public void addModifyHandlerListListener(ModifyHandlerListListener listener) {

        modifyHandlerListListeners.add(listener);
    }

    @Override
    public void removeModifyHandlerListListener(ModifyHandlerListListener listener) {

        modifyHandlerListListeners.remove(listener);
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
    public void handle(BridgeConnector source, Event event) {

        ChannelInvocation<GlobalHandleInterceptor> invocation = globalHandleChannel.invoke();
        invocation.next().handle(invocation, source, event);
    }

    private class FinalGlobalHandleInterceptor implements GlobalHandleInterceptor {

        @Override
        public void handle(ChannelInvocation<GlobalHandleInterceptor> invocation, BridgeConnector source, Event event) {

            for (Entry<EventHandler<?>, EventPredicate<?>> handler : handlers.entrySet()) {
                if (EventUtils.tryTest(handler.getValue(), event)) {
                    invokeHandlerHandleChannel(source, handler.getKey(), event);
                }
            }

            invocation.next().handle(invocation, source, event);
        }

        private void invokeHandlerHandleChannel(BridgeConnector source, EventHandler<?> handler, Event event) {

            ChannelInvocation<HandlerHandleInterceptor> newInvocation = handlerHandleChannel.invoke();
            newInvocation.next().handle(newInvocation, source, handler, event);
        }

    }

    private static class FinalHandlerHandleInterceptor implements HandlerHandleInterceptor {

        @Override
        public void handle(ChannelInvocation<HandlerHandleInterceptor> invocation, BridgeConnector source, EventHandler<?> handler, Event event) {

            EventUtils.tryHandle(handler, event);

            invocation.next().handle(invocation, source, handler, event);

        }

    }

}
