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
import java.util.concurrent.CopyOnWriteArrayList;
import com.quartercode.eventbridge.basic.AbstractBridgeModule;
import com.quartercode.eventbridge.basic.EventUtils;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.module.HandlerModule;
import com.quartercode.eventbridge.bridge.module.HandlerModule.HandleInterceptor;
import com.quartercode.eventbridge.bridge.module.LowLevelHandler;
import com.quartercode.eventbridge.bridge.module.LowLevelHandlerModule;
import com.quartercode.eventbridge.channel.Channel;
import com.quartercode.eventbridge.channel.ChannelInvocation;
import com.quartercode.eventbridge.def.channel.DefaultChannel;

/**
 * The default default implementation of the {@link LowLevelHandlerModule} interface.
 * 
 * @see LowLevelHandlerModule
 */
public class DefaultLowLevelHandlerModule extends AbstractBridgeModule implements LowLevelHandlerModule {

    private final Channel<GlobalLowLevelHandleInterceptor>   globalChannel                  = new DefaultChannel<>(GlobalLowLevelHandleInterceptor.class);
    private final Channel<SpecificLowLevelHandleInterceptor> specificChannel                = new DefaultChannel<>(SpecificLowLevelHandleInterceptor.class);

    private final HandleChannelDivertInterceptor             handleChannelDivertInterceptor = new HandleChannelDivertInterceptor();

    private final List<LowLevelHandler>                      handlers                       = new CopyOnWriteArrayList<>();
    private final List<ModifyLowLevelHandlerListListener>    modifyHandlerListListeners     = new ArrayList<>();
    private List<LowLevelHandler>                            handlersUnmodifiableCache;

    /**
     * Creates a new default low-level handler module.
     */
    public DefaultLowLevelHandlerModule() {

        globalChannel.addInterceptor(new LastGlobalLowLevelHandleInterceptor(), 0);
        specificChannel.addInterceptor(new LastSpecificLowLevelHandleInterceptor(), 0);
    }

    @Override
    public void add(Bridge bridge) {

        super.add(bridge);

        bridge.getModule(HandlerModule.class).getChannel().addInterceptor(handleChannelDivertInterceptor, 100);
    }

    @Override
    public void remove() {

        getBridge().getModule(HandlerModule.class).getChannel().removeInterceptor(handleChannelDivertInterceptor);

        super.remove();
    }

    @Override
    public List<LowLevelHandler> getHandlers() {

        if (handlersUnmodifiableCache == null) {
            handlersUnmodifiableCache = Collections.unmodifiableList(handlers);
        }

        return handlersUnmodifiableCache;
    }

    @Override
    public void addHandler(LowLevelHandler handler) {

        handlers.add(handler);
        handlersUnmodifiableCache = null;

        for (ModifyLowLevelHandlerListListener listener : modifyHandlerListListeners) {
            listener.onAddHandler(handler, this);
        }
    }

    @Override
    public void removeHandler(LowLevelHandler handler) {

        if (handlers.contains(handler)) {
            for (ModifyLowLevelHandlerListListener listener : modifyHandlerListListeners) {
                listener.onRemoveHandler(handler, this);
            }

            handlers.remove(handler);
            handlersUnmodifiableCache = null;
        }
    }

    @Override
    public void addModifyHandlerListListener(ModifyLowLevelHandlerListListener listener) {

        modifyHandlerListListeners.add(listener);
    }

    @Override
    public void removeModifyHandlerListListener(ModifyLowLevelHandlerListListener listener) {

        modifyHandlerListListeners.remove(listener);
    }

    @Override
    public Channel<GlobalLowLevelHandleInterceptor> getGlobalChannel() {

        return globalChannel;
    }

    @Override
    public Channel<SpecificLowLevelHandleInterceptor> getSpecificChannel() {

        return specificChannel;
    }

    @Override
    public void handle(Event event, BridgeConnector source) {

        ChannelInvocation<GlobalLowLevelHandleInterceptor> invocation = globalChannel.invoke();
        invocation.next().handle(invocation, event, source);
    }

    private class HandleChannelDivertInterceptor implements HandleInterceptor {

        @Override
        public void handle(ChannelInvocation<HandleInterceptor> invocation, Event event, BridgeConnector source) {

            DefaultLowLevelHandlerModule.this.handle(event, source);

            invocation.next().handle(invocation, event, source);
        }

    }

    private class LastGlobalLowLevelHandleInterceptor implements GlobalLowLevelHandleInterceptor {

        @Override
        public void handle(ChannelInvocation<GlobalLowLevelHandleInterceptor> invocation, Event event, BridgeConnector source) {

            for (LowLevelHandler handler : handlers) {
                if (EventUtils.tryTest(handler.getPredicate(), event)) {
                    invokeSpecificLowLevelHandleChannel(event, source, handler);
                }
            }

            invocation.next().handle(invocation, event, source);
        }

        private void invokeSpecificLowLevelHandleChannel(Event event, BridgeConnector source, LowLevelHandler handler) {

            ChannelInvocation<SpecificLowLevelHandleInterceptor> invocation = specificChannel.invoke();
            invocation.next().handle(invocation, event, source, handler);
        }

    }

    private static class LastSpecificLowLevelHandleInterceptor implements SpecificLowLevelHandleInterceptor {

        @Override
        public void handle(ChannelInvocation<SpecificLowLevelHandleInterceptor> invocation, Event event, BridgeConnector source, LowLevelHandler handler) {

            handler.handle(event, source);

            invocation.next().handle(invocation, event, source, handler);
        }

    }

}
