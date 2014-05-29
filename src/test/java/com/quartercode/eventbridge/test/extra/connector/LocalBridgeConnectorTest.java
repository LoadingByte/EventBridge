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

package com.quartercode.eventbridge.test.extra.connector;

import static org.junit.Assert.assertNotNull;
import java.util.concurrent.atomic.AtomicReference;
import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.action.CustomAction;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.BridgeConnectorException;
import com.quartercode.eventbridge.extra.connector.LocalBridgeConnector;
import com.quartercode.eventbridge.test.DummyEvents.EmptyEvent1;
import com.quartercode.eventbridge.test.DummyEvents.EmptyEvent2;

public class LocalBridgeConnectorTest {

    private static Action doDummyAddConnector(AtomicReference<LocalBridgeConnector> connectorStorage, Bridge localBridge) {

        return new DummyAddConnectorAction(connectorStorage, localBridge);
    }

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    private Bridge          bridge1;
    private Bridge          bridge2;

    @Before
    public void setUp() {

        bridge1 = context.mock(Bridge.class, "bridge1");
        bridge2 = context.mock(Bridge.class, "bridge2");
    }

    @Test
    public void testStart() throws BridgeConnectorException {

        final LocalBridgeConnector bridge1To2Connector = new LocalBridgeConnector(bridge2);
        final AtomicReference<LocalBridgeConnector> bridge2To1Connector = new AtomicReference<>();

        // @formatter:off
        context.checking(new Expectations() {{

            oneOf(bridge2).addConnector(with(any(LocalBridgeConnector.class)));
                will(doDummyAddConnector(bridge2To1Connector, bridge2));

        }});
        // @formatter:on

        bridge1To2Connector.start(bridge1);
        assertNotNull("Local bridge connector didn't add reverse connection", bridge2To1Connector.get());
    }

    @Test
    public void testStop() throws BridgeConnectorException {

        final LocalBridgeConnector bridge1To2Connector = new LocalBridgeConnector(bridge2);
        final AtomicReference<LocalBridgeConnector> bridge2To1Connector = new AtomicReference<>();

        // @formatter:off
        context.checking(new Expectations() {{

            // Start
            oneOf(bridge2).addConnector(with(any(LocalBridgeConnector.class)));
                will(doDummyAddConnector(bridge2To1Connector, bridge2));

        }});
        // @formatter:on

        bridge1To2Connector.start(bridge1);

        // @formatter:off
        context.checking(new Expectations() {{

            // Stop
            oneOf(bridge2).removeConnector(bridge2To1Connector.get());

        }});
        // @formatter:on

        bridge1To2Connector.stop();
    }

    @Test
    public void testStopReverse() throws BridgeConnectorException {

        final LocalBridgeConnector bridge1To2Connector = new LocalBridgeConnector(bridge2);
        final AtomicReference<LocalBridgeConnector> bridge2To1Connector = new AtomicReference<>();

        // @formatter:off
        context.checking(new Expectations() {{

            // Start
            oneOf(bridge2).addConnector(with(any(LocalBridgeConnector.class)));
                will(doDummyAddConnector(bridge2To1Connector, bridge2));

        }});
        // @formatter:on

        bridge1To2Connector.start(bridge1);

        // @formatter:off
        context.checking(new Expectations() {{

            // Stop
            oneOf(bridge1).removeConnector(bridge1To2Connector);

        }});
        // @formatter:on

        bridge2To1Connector.get().stop();
    }

    @Test
    public void testSend() throws BridgeConnectorException {

        final EmptyEvent1[] bridge1To2Events = { new EmptyEvent1(), new EmptyEvent1(), new EmptyEvent1() };
        final EmptyEvent2[] bridge2To1Events = { new EmptyEvent2(), new EmptyEvent2(), new EmptyEvent2() };

        final LocalBridgeConnector bridge1To2Connector = new LocalBridgeConnector(bridge2);
        final AtomicReference<LocalBridgeConnector> bridge2To1Connector = new AtomicReference<>();

        // @formatter:off
        context.checking(new Expectations() {{

            oneOf(bridge2).addConnector(with(any(LocalBridgeConnector.class)));
                will(doDummyAddConnector(bridge2To1Connector, bridge2));

            final Sequence handleCalls = context.sequence("handleCalls");

            // Bridge 1 -> Bridge 2 events
            oneOf(bridge2).handle(bridge1To2Events[0]); inSequence(handleCalls);
            oneOf(bridge2).handle(bridge1To2Events[1]); inSequence(handleCalls);
            oneOf(bridge2).handle(bridge1To2Events[2]); inSequence(handleCalls);

            // Bridge 2 -> Bridge 1 events
            oneOf(bridge1).handle(bridge2To1Events[0]); inSequence(handleCalls);
            oneOf(bridge1).handle(bridge2To1Events[1]); inSequence(handleCalls);
            oneOf(bridge1).handle(bridge2To1Events[2]); inSequence(handleCalls);

        }});
        // @formatter:on

        bridge1To2Connector.start(bridge1);

        // Bridge 1 -> Bridge 2 events
        bridge1To2Connector.send(bridge1To2Events[0]);
        bridge1To2Connector.send(bridge1To2Events[1]);
        bridge1To2Connector.send(bridge1To2Events[2]);

        // Bridge 2 -> Bridge 1 events
        bridge2To1Connector.get().send(bridge2To1Events[0]);
        bridge2To1Connector.get().send(bridge2To1Events[1]);
        bridge2To1Connector.get().send(bridge2To1Events[2]);
    }

    private static class DummyAddConnectorAction extends CustomAction {

        private final AtomicReference<LocalBridgeConnector> connectorStorage;
        private final Bridge                                localBridge;

        public DummyAddConnectorAction(AtomicReference<LocalBridgeConnector> connectorStorage, Bridge localBridge) {

            super("stores LocalBridgeConnector in atomic reference");

            this.connectorStorage = connectorStorage;
            this.localBridge = localBridge;
        }

        @Override
        public Object invoke(Invocation invocation) throws BridgeConnectorException {

            LocalBridgeConnector connector = (LocalBridgeConnector) invocation.getParameter(0);
            connectorStorage.set(connector);
            connector.start(localBridge);
            return null;
        }

    }

}
