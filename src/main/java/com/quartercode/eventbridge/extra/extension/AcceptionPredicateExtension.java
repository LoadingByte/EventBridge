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

package com.quartercode.eventbridge.extra.extension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;
import com.quartercode.eventbridge.basic.EventUtils;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.Bridge.ModifyConnectorListListener;
import com.quartercode.eventbridge.bridge.Bridge.ModifyHandlerListListener;
import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.EventHandler;
import com.quartercode.eventbridge.bridge.EventPredicate;
import com.quartercode.eventbridge.bridge.HandlerModule.GlobalHandleInterceptor;
import com.quartercode.eventbridge.bridge.SenderModule.ConnectorSendInterceptor;
import com.quartercode.eventbridge.bridge.SenderModule.LocalHandlerSendInterceptor;
import com.quartercode.eventbridge.channel.ChannelInvocation;

public class AcceptionPredicateExtension {

    private final Bridge                                        bridge;
    private final APEModifyHandlerListListener                  modifyHandlerListListener   = new APEModifyHandlerListListener();
    private final APEModifyConnectorListListener                modifyConnectorListListener = new APEModifyConnectorListListener();
    private final APEGlobalHandleInterceptor                    globalHandleInterceptor     = new APEGlobalHandleInterceptor();
    private final APEConnectorSendInterceptor                   connectorSendInterceptor    = new APEConnectorSendInterceptor();
    private final APELocalHandlerSendInterceptor                localHandlerSendInterceptor = new APELocalHandlerSendInterceptor();

    private final Map<BridgeConnector, List<EventPredicate<?>>> predicates                  = new HashMap<>();

    public AcceptionPredicateExtension(Bridge bridge) {

        this.bridge = bridge;

        // Listeners for sending AcceptionPredicateEvents
        bridge.addModifyHandlerListListener(modifyHandlerListListener);
        bridge.addModifyConnectorListListener(modifyConnectorListListener);

        // Global handle interceptor for receiving AcceptionPredicateEvents
        bridge.getHandlerModule().getGlobalHandleChannel().addInterceptor(globalHandleInterceptor, 50);

        // Connector send interceptor for stopping events which are not requested at the other side
        bridge.getSenderModule().getConnectorSendChannel().addInterceptor(connectorSendInterceptor, 50);

        // Local handler send interceptor for stopping APEvents from being handled locally
        bridge.getSenderModule().getLocalHandlerSendChannel().addInterceptor(localHandlerSendInterceptor, 50);
    }

    public void remove() {

        bridge.removeModifyHandlerListListener(modifyHandlerListListener);
        bridge.removeModifyConnectorListListener(modifyConnectorListListener);
        bridge.getHandlerModule().getGlobalHandleChannel().removeInterceptor(globalHandleInterceptor);
        bridge.getSenderModule().getConnectorSendChannel().removeInterceptor(connectorSendInterceptor);
        bridge.getSenderModule().getLocalHandlerSendChannel().removeInterceptor(localHandlerSendInterceptor);
    }

    private static class APEModifyHandlerListListener implements ModifyHandlerListListener {

        @Override
        public void onAddHandler(EventHandler<?> handler, EventPredicate<?> predicate, Bridge bridge) {

            bridge.send(new AcceptionPredicateEvent(new EventPredicate<?>[] { predicate }, true));
        }

        @Override
        public void onRemoveHandler(EventHandler<?> handler, EventPredicate<?> predicate, Bridge bridge) {

            bridge.send(new AcceptionPredicateEvent(new EventPredicate<?>[] { predicate }, false));
        }

    }

    private class APEModifyConnectorListListener implements ModifyConnectorListListener {

        @Override
        public void onAddConnector(BridgeConnector connector, Bridge bridge) {

            List<Pair<EventHandler<?>, EventPredicate<?>>> handlers = bridge.getHandlers();

            EventPredicate<?>[] predicates = new EventPredicate[handlers.size()];
            for (int index = 0; index < predicates.length; index++) {
                predicates[index] = handlers.get(index).getRight();
            }

            bridge.send(new AcceptionPredicateEvent(predicates, true));
        }

        @Override
        public void onRemoveConnector(BridgeConnector connector, Bridge bridge) {

            predicates.remove(connector);
        }

    }

    private class APEGlobalHandleInterceptor implements GlobalHandleInterceptor {

        @Override
        public void handle(ChannelInvocation<GlobalHandleInterceptor> invocation, BridgeConnector source, Event event) {

            if (! (event instanceof AcceptionPredicateEvent)) {
                invocation.next().handle(invocation, source, event);
                return;
            }

            // Source cannot be null since AcceptionPredicateEvents are not handled locally.
            handle(source, (AcceptionPredicateEvent) event);
        }

        private void handle(BridgeConnector connector, AcceptionPredicateEvent event) {

            if (event.isAdd()) {
                addPredicates(connector, event.getPredicates());
            } else {
                removePredicates(connector, event.getPredicates());
            }
        }

        private void addPredicates(BridgeConnector connector, EventPredicate<?>[] addPredicates) {

            List<EventPredicate<?>> connectorPredicates = predicates.get(connector);

            if (connectorPredicates == null) {
                connectorPredicates = new ArrayList<>();
                predicates.put(connector, connectorPredicates);
            }

            for (EventPredicate<?> predicate : addPredicates) {
                connectorPredicates.add(predicate);
            }
        }

        private void removePredicates(BridgeConnector connector, EventPredicate<?>[] removePredicates) {

            if (predicates.containsKey(connector)) {
                List<EventPredicate<?>> connectorPredicates = predicates.get(connector);

                for (EventPredicate<?> predicate : removePredicates) {
                    connectorPredicates.remove(predicate);
                }

                if (connectorPredicates.isEmpty()) {
                    predicates.remove(connector);
                }
            }
        }

    }

    private class APEConnectorSendInterceptor implements ConnectorSendInterceptor {

        @Override
        public void send(ChannelInvocation<ConnectorSendInterceptor> invocation, BridgeConnector connector, Event event) {

            if (event instanceof AcceptionPredicateEvent || isInteresting(connector, event)) {
                invocation.next().send(invocation, connector, event);
            }
        }

        private boolean isInteresting(BridgeConnector connector, Event event) {

            for (EventPredicate<?> predicate : predicates.get(connector)) {
                if (EventUtils.tryTest(predicate, event)) {
                    return true;
                }
            }

            return false;
        }

    }

    private class APELocalHandlerSendInterceptor implements LocalHandlerSendInterceptor {

        @Override
        public void send(ChannelInvocation<LocalHandlerSendInterceptor> invocation, Event event) {

            if (! (event instanceof AcceptionPredicateEvent)) {
                invocation.next().send(invocation, event);
            }
        }

    }

}
