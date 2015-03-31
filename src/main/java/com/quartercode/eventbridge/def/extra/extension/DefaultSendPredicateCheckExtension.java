/*
 * This file is part of EventBridge.
 * Copyright (c) 2014 QuarterCode <http://quartercode.com/>
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.quartercode.eventbridge.basic.AbstractBridgeModule;
import com.quartercode.eventbridge.basic.EventBase;
import com.quartercode.eventbridge.basic.EventUtils;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.Bridge.ModifyConnectorListListener;
import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.EventPredicate;
import com.quartercode.eventbridge.bridge.module.ConnectorSenderModule;
import com.quartercode.eventbridge.bridge.module.ConnectorSenderModule.SpecificConnectorSendInterceptor;
import com.quartercode.eventbridge.bridge.module.LocalHandlerSenderModule;
import com.quartercode.eventbridge.bridge.module.LocalHandlerSenderModule.LocalHandlerSendInterceptor;
import com.quartercode.eventbridge.bridge.module.LowLevelHandler;
import com.quartercode.eventbridge.bridge.module.LowLevelHandlerModule;
import com.quartercode.eventbridge.bridge.module.LowLevelHandlerModule.GlobalLowLevelHandleInterceptor;
import com.quartercode.eventbridge.bridge.module.LowLevelHandlerModule.ModifyLowLevelHandlerListListener;
import com.quartercode.eventbridge.channel.ChannelInvocation;
import com.quartercode.eventbridge.extra.extension.SendPredicateCheckExtension;

/**
 * The default default implementation of the {@link SendPredicateCheckExtension} interface.
 * 
 * @see SendPredicateCheckExtension
 */
public class DefaultSendPredicateCheckExtension extends AbstractBridgeModule implements SendPredicateCheckExtension {

    private final SPCEModifyLowLevelHandlerListListener         modifyLowLevelHandlerListListener = new SPCEModifyLowLevelHandlerListListener();
    private final SPCEModifyConnectorListListener               modifyConnectorListListener       = new SPCEModifyConnectorListListener();
    private final SPCEGlobalLowLevelHandleInterceptor           globalLowLevelHandleInterceptor   = new SPCEGlobalLowLevelHandleInterceptor();
    private final SPCESpecificConnectorSendInterceptor          specificConnectorSendInterceptor  = new SPCESpecificConnectorSendInterceptor();
    private final SPCELocalHandlerSendInterceptor               localHandlerSendInterceptor       = new SPCELocalHandlerSendInterceptor();

    private final Map<BridgeConnector, List<EventPredicate<?>>> predicates                        = new HashMap<>();

    @Override
    public void add(Bridge bridge) {

        super.add(bridge);

        // Listeners for sending SetPredicatesEvents
        bridge.getModule(LowLevelHandlerModule.class).addModifyHandlerListListener(modifyLowLevelHandlerListListener);
        bridge.addModifyConnectorListListener(modifyConnectorListListener);

        // Global low-level handle interceptor for receiving SetPredicatesEvents
        bridge.getModule(LowLevelHandlerModule.class).getGlobalChannel().addInterceptor(globalLowLevelHandleInterceptor, 50);

        // Connector send interceptor for stopping events which are not requested at the other side
        bridge.getModule(ConnectorSenderModule.class).getSpecificChannel().addInterceptor(specificConnectorSendInterceptor, 50);

        // Local handler send interceptor for stopping SetPredicatesEvents from being handled locally
        bridge.getModule(LocalHandlerSenderModule.class).getChannel().addInterceptor(localHandlerSendInterceptor, 50);
    }

    @Override
    public void remove() {

        getBridge().getModule(LowLevelHandlerModule.class).removeModifyHandlerListListener(modifyLowLevelHandlerListListener);
        getBridge().removeModifyConnectorListListener(modifyConnectorListListener);
        getBridge().getModule(LowLevelHandlerModule.class).getGlobalChannel().removeInterceptor(globalLowLevelHandleInterceptor);
        getBridge().getModule(ConnectorSenderModule.class).getSpecificChannel().removeInterceptor(specificConnectorSendInterceptor);
        getBridge().getModule(LocalHandlerSenderModule.class).getChannel().removeInterceptor(localHandlerSendInterceptor);

        super.remove();
    }

    private static class SPCEModifyLowLevelHandlerListListener implements ModifyLowLevelHandlerListListener {

        @Override
        public void onAddHandler(LowLevelHandler handler, LowLevelHandlerModule module) {

            module.getBridge().send(new SetPredicatesEvent(new EventPredicate<?>[] { handler.getPredicate() }, true));
        }

        @Override
        public void onRemoveHandler(LowLevelHandler handler, LowLevelHandlerModule module) {

            module.getBridge().send(new SetPredicatesEvent(new EventPredicate<?>[] { handler.getPredicate() }, false));
        }

    }

    private class SPCEModifyConnectorListListener implements ModifyConnectorListListener {

        @Override
        public void onAddConnector(BridgeConnector connector, Bridge bridge) {

            List<LowLevelHandler> handlers = bridge.getModule(LowLevelHandlerModule.class).getHandlers();

            EventPredicate<?>[] predicateArray = new EventPredicate<?>[handlers.size()];
            for (int index = 0; index < predicateArray.length; index++) {
                predicateArray[index] = handlers.get(index).getPredicate();
            }

            bridge.send(new SetPredicatesEvent(predicateArray, true));
        }

        @Override
        public void onRemoveConnector(BridgeConnector connector, Bridge bridge) {

            predicates.remove(connector);
        }

    }

    private class SPCEGlobalLowLevelHandleInterceptor implements GlobalLowLevelHandleInterceptor {

        @Override
        public void handle(ChannelInvocation<GlobalLowLevelHandleInterceptor> invocation, Event event, BridgeConnector source) {

            if (! (event instanceof SetPredicatesEvent)) {
                invocation.next().handle(invocation, event, source);
                return;
            }

            // Source cannot be null since SetPredicatesEvents are not handled locally
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

    private class SPCESpecificConnectorSendInterceptor implements SpecificConnectorSendInterceptor {

        @Override
        public void send(ChannelInvocation<SpecificConnectorSendInterceptor> invocation, Event event, BridgeConnector connector) {

            if (event instanceof SetPredicatesEvent || isInteresting(event, connector)) {
                invocation.next().send(invocation, event, connector);
            }
        }

        private boolean isInteresting(Event event, BridgeConnector connector) {

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

        private static final long         serialVersionUID = 7382589662643796833L;

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
