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

package com.quartercode.eventbridge.test.def.bridge;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.Bridge.ModifyConnectorListListener;
import com.quartercode.eventbridge.bridge.Bridge.ModifyHandlerListListener;
import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.BridgeConnectorException;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.EventHandler;
import com.quartercode.eventbridge.bridge.EventPredicate;
import com.quartercode.eventbridge.bridge.HandlerModule;
import com.quartercode.eventbridge.bridge.SenderModule;
import com.quartercode.eventbridge.def.bridge.DefaultBridge;
import com.quartercode.eventbridge.test.DummyEvents.EmptyEvent1;
import com.quartercode.eventbridge.test.DummyEvents.EmptyEvent2;

public class DefaultBridgeTest {

    private static void assertListEquals(String message, List<?> collection, Object... elements) {

        assertTrue(message, collection.size() == elements.length);

        for (int index = 0; index < collection.size(); index++) {
            assertEquals(message, elements[index], collection.get(index));
        }
    }

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    private DefaultBridge   bridge;

    @Before
    public void setUp() {

        bridge = new DefaultBridge();
    }

    @Test
    public void testGetSetHandlerModule() {

        HandlerModule handlerModule = context.mock(HandlerModule.class);
        bridge.setHandlerModule(handlerModule);

        assertEquals("Bridge handler module", handlerModule, bridge.getHandlerModule());
    }

    @Test
    public void testHandle() {

        final HandlerModule handlerModule = context.mock(HandlerModule.class);
        bridge.setHandlerModule(handlerModule);

        final EmptyEvent1 event = new EmptyEvent1();
        final BridgeConnector source = context.mock(BridgeConnector.class);

        // @formatter:off
        context.checking(new Expectations() {{

            oneOf(handlerModule).handle(source, event);

        }});
        // @formatter:on

        bridge.handle(source, event);
    }

    @Test
    public void testGetSetSenderModule() {

        SenderModule senderModule = context.mock(SenderModule.class);
        bridge.setSenderModule(senderModule);

        assertEquals("Bridge sender module", senderModule, bridge.getSenderModule());
    }

    @Test
    public void testSend() {

        final SenderModule senderModule = context.mock(SenderModule.class);
        bridge.setSenderModule(senderModule);

        final EmptyEvent1 event = new EmptyEvent1();

        // @formatter:off
        context.checking(new Expectations() {{

            oneOf(senderModule).send(event);

        }});
        // @formatter:on

        bridge.send(event);
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testHandlerStorage() {

        EventHandler<EmptyEvent1> handler1 = context.mock(EventHandler.class, "handler1");
        EventPredicate<EmptyEvent1> predicate1 = context.mock(EventPredicate.class, "predicate1");
        EventHandler<EmptyEvent2> handler2 = context.mock(EventHandler.class, "handler2");
        EventPredicate<EmptyEvent2> predicate2 = context.mock(EventPredicate.class, "predicate2");
        EventHandler<EmptyEvent2> handler3 = context.mock(EventHandler.class, "handler3");
        EventPredicate<EmptyEvent2> predicate3 = context.mock(EventPredicate.class, "predicate3");

        Pair<EventHandler<EmptyEvent1>, EventPredicate<EmptyEvent1>> pair1 = Pair.of(handler1, predicate1);
        Pair<EventHandler<EmptyEvent2>, EventPredicate<EmptyEvent2>> pair2 = Pair.of(handler2, predicate2);
        Pair<EventHandler<EmptyEvent2>, EventPredicate<EmptyEvent2>> pair3 = Pair.of(handler3, predicate3);

        assertHandlerListEmpty(bridge);

        bridge.removeHandler(handler1);
        assertHandlerListEmpty(bridge);

        bridge.addHandler(handler1, predicate1);
        assertListEquals("Handlers that are stored inside the bridge are not correct", bridge.getHandlers(), pair1);
        assertListEquals("Handlers that are stored inside the bridge changed on the second retrieval", bridge.getHandlers(), pair1);

        bridge.addHandler(handler2, predicate2);
        assertListEquals("Handlers that are stored inside the bridge are not correct", bridge.getHandlers(), pair1, pair2);
        assertListEquals("Handlers that are stored inside the bridge changed on the second retrieval", bridge.getHandlers(), pair1, pair2);

        bridge.addHandler(handler3, predicate3);
        assertListEquals("Handlers that are stored inside the bridge are not correct", bridge.getHandlers(), pair1, pair2, pair3);
        assertListEquals("Handlers that are stored inside the bridge changed on the second retrieval", bridge.getHandlers(), pair1, pair2, pair3);

        bridge.removeHandler(handler2);
        assertListEquals("Handlers that are stored inside the bridge are not correct", bridge.getHandlers(), pair1, pair3);
        assertListEquals("Handlers that are stored inside the bridge changed on the second retrieval", bridge.getHandlers(), pair1, pair3);
    }

    private void assertHandlerListEmpty(Bridge bridge) {

        assertTrue("There are handlers stored inside the bridge although none were added", bridge.getHandlers().isEmpty());
        assertTrue("Handlers that are stored inside the bridge changed on the second retrieval", bridge.getHandlers().isEmpty());
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testHandlerStorageRemoveUncheckedCast() {

        EventHandler<EmptyEvent1> handler1 = new EqualsAllHandler<>();
        EventPredicate<EmptyEvent1> predicate1 = context.mock(EventPredicate.class);
        EqualsAllHandler<EmptyEvent2> handler2 = new EqualsAllHandler<>();

        // Add handler 1 with type parameter TestEvent1
        bridge.addHandler(handler1, predicate1);

        // Remove handler 2 with type parameter TestEvent2
        // Note that handler 2 is equal to handler 1 while having a different type parameter
        bridge.removeHandler(handler2);

        assertTrue("Handler 1 wasn't removed", bridge.getHandlers().isEmpty());
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testHandlerStorageListeners() {

        final EventHandler<EmptyEvent1> handler = context.mock(EventHandler.class, "handler");
        final EventPredicate<EmptyEvent1> predicate = context.mock(EventPredicate.class, "predicate");
        final ModifyHandlerListListener listener = context.mock(ModifyHandlerListListener.class);

        // @formatter:off
        context.checking(new Expectations() {{

            final Sequence handlerCalls = context.sequence("handlerCalls");
            oneOf(listener).onAddHandler(handler,predicate, bridge); inSequence(handlerCalls);
            oneOf(listener).onRemoveHandler(handler,predicate, bridge); inSequence(handlerCalls);

        }});
        // @formatter:on

        // Calls with listener
        bridge.addModifyHandlerListListener(listener);
        bridge.addHandler(handler, predicate);
        bridge.removeHandler(handler);

        // Calls without listener
        bridge.removeModifyHandlerListListener(listener);
        bridge.addHandler(handler, predicate);
        bridge.removeHandler(handler);
    }

    @Test
    public void testConnectorStorage() throws BridgeConnectorException {

        final BridgeConnector connector1 = context.mock(BridgeConnector.class, "connector1");
        final BridgeConnector connector2 = context.mock(BridgeConnector.class, "connector2");

        // @formatter:off
        context.checking(new Expectations() {{

            final Sequence connectorCalls = context.sequence("connectorCalls");
            oneOf(connector1).start(bridge); inSequence(connectorCalls);
            oneOf(connector2).start(bridge); inSequence(connectorCalls);
            oneOf(connector1).stop(); inSequence(connectorCalls);

        }});
        // @formatter:on

        bridge.addConnector(connector1);
        assertListEquals("Connectors that are stored inside the bridge are not correct", bridge.getConnectors(), connector1);
        assertListEquals("Connectors that are stored inside the bridge changed on the second retrieval", bridge.getConnectors(), connector1);

        bridge.addConnector(connector2);
        assertListEquals("Connectors that are stored inside the bridge are not correct", bridge.getConnectors(), connector1, connector2);
        assertListEquals("Connectors that are stored inside the bridge changed on the second retrieval", bridge.getConnectors(), connector1, connector2);

        bridge.removeConnector(connector1);
        assertListEquals("Connectors that are stored inside the bridge are not correct", bridge.getConnectors(), connector2);
        assertListEquals("Connectors that are stored inside the bridge changed on the second retrieval", bridge.getConnectors(), connector2);
    }

    @Test
    public void testConnectorStorageListeners() throws BridgeConnectorException {

        final BridgeConnector connector = context.mock(BridgeConnector.class);
        final ModifyConnectorListListener listener = context.mock(ModifyConnectorListListener.class);

        // @formatter:off
        context.checking(new Expectations() {{

            final Sequence calls = context.sequence("calls");

            // Calls with listener
            oneOf(connector).start(bridge); inSequence(calls);
            oneOf(listener).onAddConnector(connector, bridge); inSequence(calls);
            oneOf(listener).onRemoveConnector(connector, bridge); inSequence(calls);
            oneOf(connector).stop(); inSequence(calls);

            // Calls without listener
            oneOf(connector).start(bridge); inSequence(calls);
            oneOf(connector).stop(); inSequence(calls);

        }});
        // @formatter:on

        // Calls with listener
        bridge.addModifyConnectorListListener(listener);
        bridge.addConnector(connector);
        bridge.removeConnector(connector);

        // Calls without listener
        bridge.removeModifyConnectorListListener(listener);
        bridge.addConnector(connector);
        bridge.removeConnector(connector);
    }

    private static class EqualsAllHandler<T extends Event> implements EventHandler<T> {

        @Override
        public boolean equals(Object obj) {

            return true;
        }

        @Override
        public void handle(T event) {

            // Not used
        }

    }

}
