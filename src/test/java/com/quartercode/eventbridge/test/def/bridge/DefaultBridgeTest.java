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

import static com.quartercode.eventbridge.test.ExtraAssert.assertListEquals;
import static org.junit.Assert.*;
import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import com.quartercode.eventbridge.bridge.Bridge.ModifyConnectorListListener;
import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.BridgeConnectorException;
import com.quartercode.eventbridge.bridge.BridgeModule;
import com.quartercode.eventbridge.bridge.module.ConnectorSenderModule;
import com.quartercode.eventbridge.bridge.module.HandlerModule;
import com.quartercode.eventbridge.bridge.module.LocalHandlerSenderModule;
import com.quartercode.eventbridge.bridge.module.LowLevelHandlerModule;
import com.quartercode.eventbridge.bridge.module.SenderModule;
import com.quartercode.eventbridge.bridge.module.StandardHandlerModule;
import com.quartercode.eventbridge.def.bridge.DefaultBridge;
import com.quartercode.eventbridge.test.DummyEvents.EmptyEvent1;

public class DefaultBridgeTest {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    private DefaultBridge   bridge;

    @Before
    public void setUp() {

        bridge = new DefaultBridge();
    }

    @Test
    public void testAddModule() {

        final BridgeModule1 module = context.mock(BridgeModule1.class);

        // @formatter:off
        context.checking(new Expectations() {{

            oneOf(module).add(bridge);

        }});
        // @formatter:on

        bridge.addModule(module);

        assertEquals("Retrieved bridge module (by interface)", module, bridge.getModule(BridgeModule1.class));
        assertEquals("Retrieved bridge module (by class)", module, bridge.getModule(module.getClass()));
    }

    @Test (expected = NullPointerException.class)
    public void testAddNullModule() {

        bridge.addModule(null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testAddModuleTypeTwice() {

        final BridgeModule1 module1 = context.mock(BridgeModule1.class, "module1");
        BridgeModule1 module2 = context.mock(BridgeModule1.class, "module2");

        // @formatter:off
        context.checking(new Expectations() {{

            oneOf(module1).add(bridge);

        }});
        // @formatter:on

        bridge.addModule(module1);
        bridge.addModule(module2);
    }

    @Test
    public void testAddMultipleModules() {

        final BridgeModule module1 = context.mock(BridgeModule1.class);
        final BridgeModule module2 = context.mock(BridgeModule2.class);

        // @formatter:off
        context.checking(new Expectations() {{

            final Sequence moduleCalls = context.sequence("moduleCalls");

            oneOf(module1).add(bridge); inSequence(moduleCalls);
            oneOf(module2).add(bridge); inSequence(moduleCalls);

        }});
        // @formatter:on

        bridge.addModule(module1);
        bridge.addModule(module2);

        assertEquals("Retrieved bridge module", module1, bridge.getModule(BridgeModule1.class));
        assertEquals("Retrieved bridge module", module2, bridge.getModule(BridgeModule2.class));
    }

    @Test
    public void testRemoveModule() {

        final BridgeModule module = context.mock(BridgeModule.class);

        // @formatter:off
        context.checking(new Expectations() {{

            final Sequence moduleCalls = context.sequence("moduleCalls");

            oneOf(module).add(bridge); inSequence(moduleCalls);
            oneOf(module).remove(); inSequence(moduleCalls);

        }});
        // @formatter:on

        bridge.addModule(module);
        bridge.removeModule(module);
    }

    @Test
    public void testRemoveUnknownModules() {

        BridgeModule module = context.mock(BridgeModule1.class);

        assertNull("Bridge module exists although it wasn't added", bridge.getModule(BridgeModule1.class));
        bridge.removeModule(module);
        assertNull("Bridge module exists although it wasn't added", bridge.getModule(BridgeModule1.class));
    }

    @Test
    public void testRemoveMultipleModules() {

        final BridgeModule module1 = context.mock(BridgeModule1.class);
        final BridgeModule module2 = context.mock(BridgeModule2.class);

        // @formatter:off
        context.checking(new Expectations() {{

            final Sequence moduleCalls = context.sequence("moduleCalls");

            oneOf(module1).add(bridge); inSequence(moduleCalls);
            oneOf(module2).add(bridge); inSequence(moduleCalls);

            oneOf(module2).remove(); inSequence(moduleCalls);
            oneOf(module1).remove(); inSequence(moduleCalls);

        }});
        // @formatter:on

        bridge.addModule(module1);
        assertEquals("Retrieved bridge module", module1, bridge.getModule(BridgeModule1.class));
        bridge.addModule(module2);
        assertEquals("Retrieved bridge module", module2, bridge.getModule(BridgeModule2.class));

        bridge.removeModule(module2);
        assertNull("Bridge module exists although it was removed", bridge.getModule(BridgeModule2.class));
        bridge.removeModule(module1);
        assertNull("Bridge module exists although it was removed", bridge.getModule(BridgeModule1.class));
    }

    @Test
    public void testSenderModulesAdded() {

        assertNotNull("New bridge doesn't have a sender module", bridge.getModule(SenderModule.class));
        assertNotNull("New bridge doesn't have a connector sender module", bridge.getModule(ConnectorSenderModule.class));
        assertNotNull("New bridge doesn't have a local handler sender module", bridge.getModule(LocalHandlerSenderModule.class));
    }

    @Test
    public void testHandlerModulesAdded() {

        assertNotNull("New bridge doesn't have a handler module", bridge.getModule(HandlerModule.class));
        assertNotNull("New bridge doesn't have a low-level handler module", bridge.getModule(LowLevelHandlerModule.class));
        assertNotNull("New bridge doesn't have a standard handler module", bridge.getModule(StandardHandlerModule.class));
    }

    @Test
    public void testSend() {

        final SenderModule senderModule = context.mock(SenderModule.class);
        final EmptyEvent1 event = new EmptyEvent1();

        // @formatter:off
        context.checking(new Expectations() {{

            allowing(senderModule).add(bridge);
            oneOf(senderModule).send(event);

        }});
        // @formatter:on

        // Replace the bridge's sender module with a mock object
        bridge.removeModule(bridge.getModule(SenderModule.class));
        bridge.addModule(senderModule);

        bridge.send(event);
    }

    @Test
    public void testHandle() {

        final HandlerModule handlerModule = context.mock(HandlerModule.class);
        final EmptyEvent1 event = new EmptyEvent1();
        final BridgeConnector source = context.mock(BridgeConnector.class);

        // @formatter:off
        context.checking(new Expectations() {{

            allowing(handlerModule).add(bridge);
            oneOf(handlerModule).handle(event, source);

        }});
        // @formatter:on

        // Replace the bridge's handler module with a mock object
        bridge.removeModule(bridge.getModule(HandlerModule.class));
        bridge.addModule(handlerModule);

        bridge.handle(event, source);
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

        // Remove connector which wasn't added before
        assertTrue("There are connectors stored inside the bridge although none were added", bridge.getConnectors().isEmpty());
        bridge.removeConnector(connector1);
        assertTrue("There are connectors stored inside the bridge although none were added", bridge.getConnectors().isEmpty());

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

    @Test (expected = BridgeConnectorException.class)
    public void testConnectorStorageAddErrors() throws BridgeConnectorException {

        final BridgeConnector connector = context.mock(BridgeConnector.class, "connector");

        // @formatter:off
        context.checking(new Expectations() {{

            oneOf(connector).start(bridge);
                will(throwException(new BridgeConnectorException(connector)));

        }});
        // @formatter:on

        try {
            bridge.addConnector(connector);
        } finally {
            assertTrue("Bridge connector which threw an error during adding was nevertheless added", bridge.getConnectors().isEmpty());
        }
    }

    @Test (expected = BridgeConnectorException.class)
    public void testConnectorStorageRemoveErrors() throws BridgeConnectorException {

        final BridgeConnector connector = context.mock(BridgeConnector.class, "connector");

        // @formatter:off
        context.checking(new Expectations() {{

            oneOf(connector).start(bridge);
            oneOf(connector).stop();
                will(throwException(new BridgeConnectorException(connector)));

        }});
        // @formatter:on

        bridge.addConnector(connector);

        try {
            bridge.removeConnector(connector);
        } finally {
            assertTrue("Bridge connector which threw an error during removal wasn't actually removed", bridge.getConnectors().isEmpty());
        }
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

    private static interface BridgeModule1 extends BridgeModule {

    }

    private static interface BridgeModule2 extends BridgeModule {

    }

}
