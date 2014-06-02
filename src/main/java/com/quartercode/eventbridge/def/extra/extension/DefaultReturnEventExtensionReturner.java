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

package com.quartercode.eventbridge.def.extra.extension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.quartercode.eventbridge.basic.AbstractBridgeModule;
import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.BridgeConnectorException;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.EventPredicate;
import com.quartercode.eventbridge.bridge.module.LowLevelHandler;
import com.quartercode.eventbridge.bridge.module.LowLevelHandlerModule;
import com.quartercode.eventbridge.channel.Channel;
import com.quartercode.eventbridge.channel.ChannelInvocation;
import com.quartercode.eventbridge.def.channel.DefaultChannel;
import com.quartercode.eventbridge.def.extra.extension.ReturnEventExtensionWrapper.ReturnEventExtensionWrapperPredicate;
import com.quartercode.eventbridge.extra.extension.RequestEventHandler;
import com.quartercode.eventbridge.extra.extension.ReturnEventExtensionReturner;
import com.quartercode.eventbridge.extra.extension.ReturnEventSender;

/**
 * The default default implementation of the {@link ReturnEventExtensionReturner} interface.
 * 
 * @see ReturnEventExtensionReturner
 */
public class DefaultReturnEventExtensionReturner extends AbstractBridgeModule implements ReturnEventExtensionReturner {

    private static final Logger                                  LOGGER                            = LoggerFactory.getLogger(DefaultReturnEventExtensionReturner.class);

    private final Channel<RequestHandleInterceptor>              requestHandleChannel              = new DefaultChannel<>(RequestHandleInterceptor.class);

    private final Map<RequestEventHandler<?>, EventPredicate<?>> requestHandlers                   = new ConcurrentHashMap<>();
    private final Map<RequestEventHandler<?>, LowLevelHandler>   lowLevelRequestHandlers           = new ConcurrentHashMap<>();
    private final List<ModifyRequestHandlerListListener>         modifyRequestHandlerListListeners = new ArrayList<>();
    private Map<RequestEventHandler<?>, EventPredicate<?>>       requestHandlersUnmodifiableCache;

    /**
     * Creates a new return event extension returner.
     * See the {@link ReturnEventExtensionReturner} javadoc for more details on how to use the extension.
     */
    public DefaultReturnEventExtensionReturner() {

        requestHandleChannel.addInterceptor(new LastRequestHandleInterceptor(), 0);
    }

    @Override
    public void remove() {

        for (LowLevelHandler lowLevelHandler : lowLevelRequestHandlers.values()) {
            getBridge().getModule(LowLevelHandlerModule.class).removeHandler(lowLevelHandler);
        }

        super.remove();
    }

    @Override
    public Map<RequestEventHandler<?>, EventPredicate<?>> getRequestHandlers() {

        if (requestHandlersUnmodifiableCache == null) {
            requestHandlersUnmodifiableCache = Collections.unmodifiableMap(requestHandlers);
        }

        return requestHandlersUnmodifiableCache;
    }

    @Override
    public void addRequestHandler(RequestEventHandler<?> requestHandler, EventPredicate<?> predicate) {

        requestHandlers.put(requestHandler, predicate);
        requestHandlersUnmodifiableCache = null;

        EventPredicate<?> wrapperPredicate = new ReturnEventExtensionWrapperPredicate(predicate);
        LowLevelHandler lowLevelHandler = new LowLevelHandlerAdapter(requestHandler, wrapperPredicate);
        lowLevelRequestHandlers.put(requestHandler, lowLevelHandler);
        getBridge().getModule(LowLevelHandlerModule.class).addHandler(lowLevelHandler);

        for (ModifyRequestHandlerListListener listener : modifyRequestHandlerListListeners) {
            listener.onAddRequestHandler(requestHandler, predicate, this);
        }
    }

    @Override
    public void removeRequestHandler(RequestEventHandler<?> requestHandler) {

        if (requestHandlers.containsKey(requestHandler)) {
            if (!modifyRequestHandlerListListeners.isEmpty()) {
                EventPredicate<?> predicate = requestHandlers.get(requestHandler);
                for (ModifyRequestHandlerListListener listener : modifyRequestHandlerListListeners) {
                    listener.onRemoveRequestHandler(requestHandler, predicate, this);
                }
            }

            LowLevelHandler lowLevelHandler = lowLevelRequestHandlers.get(requestHandler);
            lowLevelRequestHandlers.remove(requestHandler);
            getBridge().getModule(LowLevelHandlerModule.class).removeHandler(lowLevelHandler);

            requestHandlers.remove(requestHandler);
            requestHandlersUnmodifiableCache = null;
        }
    }

    @Override
    public void addModifyRequestHandlerListListener(ModifyRequestHandlerListListener listener) {

        modifyRequestHandlerListListeners.add(listener);
    }

    @Override
    public void removeModifyRequestHandlerListListener(ModifyRequestHandlerListListener listener) {

        modifyRequestHandlerListListeners.remove(listener);
    }

    @Override
    public Channel<RequestHandleInterceptor> getRequestHandleChannel() {

        return requestHandleChannel;
    }

    private class LowLevelHandlerAdapter implements LowLevelHandler {

        private final RequestEventHandler<?> requestHandler;
        private final EventPredicate<?>      predicate;

        private LowLevelHandlerAdapter(RequestEventHandler<?> requestHandler, EventPredicate<?> predicate) {

            this.requestHandler = requestHandler;
            this.predicate = predicate;
        }

        @Override
        public EventPredicate<?> getPredicate() {

            return predicate;
        }

        @Override
        public void handle(Event event, BridgeConnector source) {

            ReturnEventExtensionWrapper wrapper = (ReturnEventExtensionWrapper) event;

            ReturnEventSender returnSender = null;
            if (source == null) {
                returnSender = new LocalBridgeReturnEventSender(wrapper.getRequestId());
            } else {
                returnSender = new BridgeConnectorReturnEventSender(wrapper.getRequestId(), source);
            }

            ChannelInvocation<RequestHandleInterceptor> invocation = requestHandleChannel.invoke();
            invocation.next().handleRequest(invocation, wrapper.getEvent(), source, requestHandler, returnSender);
        }

        private class LocalBridgeReturnEventSender implements ReturnEventSender {

            private final long requestId;

            private LocalBridgeReturnEventSender(long requestId) {

                this.requestId = requestId;
            }

            @Override
            public void send(Event event) {

                getBridge().handle(new ReturnEventExtensionWrapper(event, requestId, false), null);
            }

        }

        private class BridgeConnectorReturnEventSender implements ReturnEventSender {

            private final long            requestId;
            private final BridgeConnector connector;

            private BridgeConnectorReturnEventSender(long requestId, BridgeConnector connector) {

                this.requestId = requestId;
                this.connector = connector;
            }

            @Override
            public void send(Event event) {

                try {
                    connector.send(new ReturnEventExtensionWrapper(event, requestId, false));
                } catch (BridgeConnectorException e) {
                    LOGGER.error("Can't send return event '{}' back through bridge connector '{}'", event, connector, e);
                }
            }

        }

    }

    private class LastRequestHandleInterceptor implements RequestHandleInterceptor {

        @Override
        public void handleRequest(ChannelInvocation<RequestHandleInterceptor> invocation, Event request, BridgeConnector source, RequestEventHandler<?> requestHandler, ReturnEventSender returnSender) {

            tryHandle(requestHandler, request, returnSender);

            invocation.next().handleRequest(invocation, request, source, requestHandler, returnSender);
        }

        private <T extends Event> void tryHandle(RequestEventHandler<T> handler, Event event, ReturnEventSender sender) {

            try {
                @SuppressWarnings ("unchecked")
                T castedEvent = (T) event;
                handler.handle(castedEvent, sender);
            } catch (ClassCastException e) {
                // Do nothing
            }
        }

    }

}
