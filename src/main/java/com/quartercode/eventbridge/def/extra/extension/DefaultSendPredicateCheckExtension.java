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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.tuple.Pair;
import com.quartercode.eventbridge.basic.AbstractBridgeModule;
import com.quartercode.eventbridge.basic.EventBase;
import com.quartercode.eventbridge.basic.EventUtils;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.Bridge.ModifyConnectorListListener;
import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.EventHandler;
import com.quartercode.eventbridge.bridge.EventPredicate;
import com.quartercode.eventbridge.bridge.HandlerModule;
import com.quartercode.eventbridge.bridge.HandlerModule.GlobalHandleInterceptor;
import com.quartercode.eventbridge.bridge.HandlerModule.ModifyHandlerListListener;
import com.quartercode.eventbridge.bridge.SenderModule;
import com.quartercode.eventbridge.bridge.SenderModule.ConnectorSendInterceptor;
import com.quartercode.eventbridge.bridge.SenderModule.LocalHandlerSendInterceptor;
import com.quartercode.eventbridge.channel.ChannelInvocation;
import com.quartercode.eventbridge.extra.extension.SendPredicateCheckExtension;

/**
 * The default default implementation of the {@link SendPredicateCheckExtension} interface.
 * 
 * @see SendPredicateCheckExtension
 */
public class DefaultSendPredicateCheckExtension extends AbstractBridgeModule implements SendPredicateCheckExtension {

    private final SPCEModifyHandlerListListener                 modifyHandlerListListener   = new SPCEModifyHandlerListListener();
    private final SPCEModifyConnectorListListener               modifyConnectorListListener = new SPCEModifyConnectorListListener();
    private final SPCEGlobalHandleInterceptor                   globalHandleInterceptor     = new SPCEGlobalHandleInterceptor();
    private final SPCEConnectorSendInterceptor                  connectorSendInterceptor    = new SPCEConnectorSendInterceptor();
    private final SPCELocalHandlerSendInterceptor               localHandlerSendInterceptor = new SPCELocalHandlerSendInterceptor();

    private final Map<BridgeConnector, List<EventPredicate<?>>> predicates                  = new HashMap<>();

    /**
     * Creates a new send predicate check extension.
     * See the {@link SendPredicateCheckExtension} javadoc for more details on how to use the extension.
     */
    public DefaultSendPredicateCheckExtension() {

    }

    @Override
    public void add(Bridge bridge) {

        super.add(bridge);

        // Listeners for sending SetPredicatesEvents
        bridge.getModule(HandlerModule.class).addModifyHandlerListListener(modifyHandlerListListener);
        bridge.addModifyConnectorListListener(modifyConnectorListListener);

        // Global handle interceptor for receiving SetPredicatesEvents
        bridge.getModule(HandlerModule.class).getGlobalHandleChannel().addInterceptor(globalHandleInterceptor, 50);

        // Connector send interceptor for stopping events which are not requested at the other side
        bridge.getModule(SenderModule.class).getConnectorSendChannel().addInterceptor(connectorSendInterceptor, 50);

        // Local handler send interceptor for stopping SetPredicatesEvents from being handled locally
        bridge.getModule(SenderModule.class).getLocalHandlerSendChannel().addInterceptor(localHandlerSendInterceptor, 50);
    }

    @Override
    public void remove() {

        getBridge().getModule(HandlerModule.class).removeModifyHandlerListListener(modifyHandlerListListener);
        getBridge().removeModifyConnectorListListener(modifyConnectorListListener);
        getBridge().getModule(HandlerModule.class).getGlobalHandleChannel().removeInterceptor(globalHandleInterceptor);
        getBridge().getModule(SenderModule.class).getConnectorSendChannel().removeInterceptor(connectorSendInterceptor);
        getBridge().getModule(SenderModule.class).getLocalHandlerSendChannel().removeInterceptor(localHandlerSendInterceptor);

        super.remove();
    }

    private static class SPCEModifyHandlerListListener implements ModifyHandlerListListener {

        @Override
        public void onAddHandler(EventHandler<?> handler, EventPredicate<?> predicate, HandlerModule handlerModule) {

            handlerModule.getBridge().send(new SetPredicatesEvent(new EventPredicate<?>[] { predicate }, true));
        }

        @Override
        public void onRemoveHandler(EventHandler<?> handler, EventPredicate<?> predicate, HandlerModule handlerModule) {

            handlerModule.getBridge().send(new SetPredicatesEvent(new EventPredicate<?>[] { predicate }, false));
        }

    }

    private class SPCEModifyConnectorListListener implements ModifyConnectorListListener {

        @Override
        public void onAddConnector(BridgeConnector connector, Bridge bridge) {

            Collection<EventPredicate<?>> predicateCollection = bridge.getModule(HandlerModule.class).getHandlers().values();
            EventPredicate<?>[] predicateArray = predicateCollection.toArray(new EventPredicate<?>[predicateCollection.size()]);

            bridge.send(new SetPredicatesEvent(predicateArray, true));
        }

        @Override
        public void onRemoveConnector(BridgeConnector connector, Bridge bridge) {

            predicates.remove(connector);
        }

    }

    private class SPCEGlobalHandleInterceptor implements GlobalHandleInterceptor {

        @Override
        public void handle(ChannelInvocation<GlobalHandleInterceptor> invocation, BridgeConnector source, Event event) {

            if (! (event instanceof SetPredicatesEvent)) {
                invocation.next().handle(invocation, source, event);
                return;
            }

            // Source cannot be null since SetPredicatesEvents are not handled locally.
            handle(source, (SetPredicatesEvent) event);
        }

        private void handle(BridgeConnector connector, SetPredicatesEvent event) {

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

    private class SPCEConnectorSendInterceptor implements ConnectorSendInterceptor {

        @Override
        public void send(ChannelInvocation<ConnectorSendInterceptor> invocation, BridgeConnector connector, Event event) {

            if (event instanceof SetPredicatesEvent || isInteresting(connector, event)) {
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

    private static class SPCELocalHandlerSendInterceptor implements LocalHandlerSendInterceptor {

        @Override
        public void send(ChannelInvocation<LocalHandlerSendInterceptor> invocation, Event event) {

            if (! (event instanceof SetPredicatesEvent)) {
                invocation.next().send(invocation, event);
            }
        }

    }

    private static class SetPredicatesEvent extends EventBase {

        private static final long         serialVersionUID = 5988984260797972075L;

        private final EventPredicate<?>[] predicates;
        private final boolean             add;

        private SetPredicatesEvent(EventPredicate<?>[] predicates, boolean add) {

            this.predicates = predicates.clone();
            this.add = add;
        }

        private EventPredicate<?>[] getPredicates() {

            return predicates.clone();
        }

        private boolean isAdd() {

            return add;
        }

    }

}
