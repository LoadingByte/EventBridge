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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.commons.lang3.tuple.Pair;
import com.quartercode.eventbridge.basic.AbstractBridgeModule;
import com.quartercode.eventbridge.basic.EventUtils;
import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.EventHandler;
import com.quartercode.eventbridge.bridge.EventPredicate;
import com.quartercode.eventbridge.bridge.HandlerModule;
import com.quartercode.eventbridge.channel.Channel;
import com.quartercode.eventbridge.channel.ChannelInvocation;
import com.quartercode.eventbridge.def.channel.DefaultChannel;

/**
 * The default default implementation of the {@link DefaultHandlerModule} interface.
 * 
 * @see HandlerModule
 */
public class DefaultHandlerModule extends AbstractBridgeModule implements HandlerModule {

    private final List<Pair<EventHandler<?>, EventPredicate<?>>> handlers                   = new CopyOnWriteArrayList<>();
    private final List<ModifyHandlerListListener>                modifyHandlerListListeners = new ArrayList<>();
    private List<Pair<EventHandler<?>, EventPredicate<?>>>       handlersUnmodifiableCache;

    private final Channel<GlobalHandleInterceptor>               globalHandleChannel        = new DefaultChannel<>(GlobalHandleInterceptor.class);
    private final Channel<HandlerHandleInterceptor>              handlerHandleChannel       = new DefaultChannel<>(HandlerHandleInterceptor.class);

    /**
     * Creates a new default sender module.
     */
    public DefaultHandlerModule() {

        globalHandleChannel.addInterceptor(new FinalGlobalHandleInterceptor(), 0);
        handlerHandleChannel.addInterceptor(new FinalHandlerHandleInterceptor(), 0);
    }

    @Override
    public List<Pair<EventHandler<?>, EventPredicate<?>>> getHandlers() {

        if (handlersUnmodifiableCache == null) {
            handlersUnmodifiableCache = Collections.unmodifiableList(handlers);
        }

        return handlersUnmodifiableCache;
    }

    @Override
    public void addHandler(EventHandler<?> handler, EventPredicate<?> predicate) {

        handlers.add(Pair.<EventHandler<?>, EventPredicate<?>> of(handler, predicate));
        handlersUnmodifiableCache = null;

        for (ModifyHandlerListListener listener : modifyHandlerListListeners) {
            listener.onAddHandler(handler, predicate, this);
        }
    }

    @Override
    public void removeHandler(EventHandler<?> handler) {

        Pair<EventHandler<?>, EventPredicate<?>> pair = null;
        for (Pair<EventHandler<?>, EventPredicate<?>> testPair : handlers) {
            if (testPair.getLeft().equals(handler)) {
                pair = testPair;
                break;
            }
        }

        if (pair != null) {
            if (!modifyHandlerListListeners.isEmpty()) {
                for (ModifyHandlerListListener listener : modifyHandlerListListeners) {
                    listener.onRemoveHandler(pair.getLeft(), pair.getRight(), this);
                }
            }

            handlers.remove(pair);
            handlersUnmodifiableCache = null;

            return;
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

            for (Pair<EventHandler<?>, EventPredicate<?>> handler : handlers) {
                if (EventUtils.tryTest(handler.getRight(), event)) {
                    invokeHandlerHandleChannel(source, handler.getLeft(), event);
                }
            }

            invocation.next().handle(invocation, source, event);
        }

        private void invokeHandlerHandleChannel(BridgeConnector source, EventHandler<?> handler, Event event) {

            ChannelInvocation<HandlerHandleInterceptor> newInvocation = handlerHandleChannel.invoke();
            newInvocation.next().handle(newInvocation, source, handler, event);
        }

    }

    /*
     * This class is static because it doesn't need a reference to DefaultHandlerModule.
     * -> Better performance
     */
    private static class FinalHandlerHandleInterceptor implements HandlerHandleInterceptor {

        @Override
        public void handle(ChannelInvocation<HandlerHandleInterceptor> invocation, BridgeConnector source, EventHandler<?> handler, Event event) {

            EventUtils.tryHandle(handler, event);

            invocation.next().handle(invocation, source, handler, event);

        }

    }

}
