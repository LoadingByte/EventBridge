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
import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import com.quartercode.eventbridge.bridge.Bridge.ModifyConnectorListListener;
import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.BridgeConnectorException;
import com.quartercode.eventbridge.bridge.SenderModule;
import com.quartercode.eventbridge.def.bridge.DefaultBridge;

public class DefaultBridgeTest {

    @SuppressWarnings ("unchecked")
    private static <T> void assertListEquals(String message, List<T> collection, T... elements) {

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
    public void testGetSenderModule() {

        SenderModule senderModule = context.mock(SenderModule.class);
        bridge.setSenderModule(senderModule);

        assertEquals("Bridge sender module", senderModule, bridge.getSenderModule());
    }

    @Test
    public void testSend() {

        final SenderModule senderModule = context.mock(SenderModule.class);
        bridge.setSenderModule(senderModule);

        final EmptyEvent event = new EmptyEvent();

        // @formatter:off
        context.checking(new Expectations() {{

            oneOf(senderModule).send(event);

        }});
        // @formatter:on

        bridge.send(event);
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

}
