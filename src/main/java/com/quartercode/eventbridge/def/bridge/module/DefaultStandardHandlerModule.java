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
import java.util.concurrent.ConcurrentHashMap;
import com.quartercode.eventbridge.basic.AbstractBridgeModule;
import com.quartercode.eventbridge.basic.EventUtils;
import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.EventPredicate;
import com.quartercode.eventbridge.bridge.module.EventHandler;
import com.quartercode.eventbridge.bridge.module.HandlerModule;
import com.quartercode.eventbridge.bridge.module.LowLevelHandler;
import com.quartercode.eventbridge.bridge.module.LowLevelHandlerModule;
import com.quartercode.eventbridge.bridge.module.StandardHandlerModule;
import com.quartercode.eventbridge.channel.Channel;
import com.quartercode.eventbridge.channel.ChannelInvocation;
import com.quartercode.eventbridge.def.channel.DefaultChannel;

/**
 * The default default implementation of the {@link DefaultStandardHandlerModule} interface.
 * 
 * @see HandlerModule
 */
public class DefaultStandardHandlerModule extends AbstractBridgeModule implements StandardHandlerModule {

    private final Channel<StandardHandleInterceptor>      channel                    = new DefaultChannel<>(StandardHandleInterceptor.class);

    private final Map<EventHandler<?>, EventPredicate<?>> handlers                   = new ConcurrentHashMap<>();
    private final Map<EventHandler<?>, LowLevelHandler>   lowLevelHandlers           = new ConcurrentHashMap<>();
    private final List<ModifyStandardHandlerListListener> modifyHandlerListListeners = new ArrayList<>();
    private Map<EventHandler<?>, EventPredicate<?>>       handlersUnmodifiableCache;

    /**
     * Creates a new default standard handler module.
     */
    public DefaultStandardHandlerModule() {

        channel.addInterceptor(new LastStandardHandleInterceptor(), 0);
    }

    @Override
    public void remove() {

        for (LowLevelHandler lowLevelHandler : lowLevelHandlers.values()) {
            getBridge().getModule(LowLevelHandlerModule.class).removeHandler(lowLevelHandler);
        }

        super.remove();
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

        LowLevelHandler lowLevelHandler = new LowLevelHandlerAdapter(handler, predicate);
        lowLevelHandlers.put(handler, lowLevelHandler);
        getBridge().getModule(LowLevelHandlerModule.class).addHandler(lowLevelHandler);

        for (ModifyStandardHandlerListListener listener : modifyHandlerListListeners) {
            listener.onAddHandler(handler, predicate, this);
        }
    }

    @Override
    public void removeHandler(EventHandler<?> handler) {

        if (handlers.containsKey(handler)) {
            if (!modifyHandlerListListeners.isEmpty()) {
                EventPredicate<?> predicate = handlers.get(handler);
                for (ModifyStandardHandlerListListener listener : modifyHandlerListListeners) {
                    listener.onRemoveHandler(handler, predicate, this);
                }
            }

            LowLevelHandler lowLevelHandler = lowLevelHandlers.get(handler);
            lowLevelHandlers.remove(handler);
            getBridge().getModule(LowLevelHandlerModule.class).removeHandler(lowLevelHandler);

            handlers.remove(handler);
            handlersUnmodifiableCache = null;
        }
    }

    @Override
    public void addModifyHandlerListListener(ModifyStandardHandlerListListener listener) {

        modifyHandlerListListeners.add(listener);
    }

    @Override
    public void removeModifyHandlerListListener(ModifyStandardHandlerListListener listener) {

        modifyHandlerListListeners.remove(listener);
    }

    @Override
    public Channel<StandardHandleInterceptor> getChannel() {

        return channel;
    }

    private void handle(Event event, BridgeConnector source, EventHandler<?> handler) {

        ChannelInvocation<StandardHandleInterceptor> invocation = channel.invoke();
        invocation.next().handle(invocation, event, source, handler);
    }

    private class LowLevelHandlerAdapter implements LowLevelHandler {

        private final EventHandler<?>   handler;
        private final EventPredicate<?> predicate;

        private LowLevelHandlerAdapter(EventHandler<?> handler, EventPredicate<?> predicate) {

            this.handler = handler;
            this.predicate = predicate;
        }

        @Override
        public EventPredicate<?> getPredicate() {

            return predicate;
        }

        @Override
        public void handle(Event event, BridgeConnector source) {

            DefaultStandardHandlerModule.this.handle(event, source, handler);
        }

    }

    private static class LastStandardHandleInterceptor implements StandardHandleInterceptor {

        @Override
        public void handle(ChannelInvocation<StandardHandleInterceptor> invocation, Event event, BridgeConnector source, EventHandler<?> handler) {

            EventUtils.tryHandle(handler, event);

            invocation.next().handle(invocation, event, source, handler);
        }

    }

}
