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

package com.quartercode.eventbridge.test.basic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import com.quartercode.eventbridge.basic.AbstractBridgeConnector;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.BridgeConnectorException;
import com.quartercode.eventbridge.bridge.Event;

public class AbstractBridgeConnectorTest {

    @Rule
    public JUnitRuleMockery             context = new JUnitRuleMockery();

    private TestAbstractBridgeConnector connector;

    @Before
    public void setUp() {

        connector = new TestAbstractBridgeConnector();
    }

    @Test
    public void testStart() throws BridgeConnectorException {

        Bridge localBridge = context.mock(Bridge.class);

        connector.start(localBridge);

        assertEquals("Local bridge that is stored by the connector", localBridge, connector.getLocalBridge());
    }

    @Test (expected = BridgeConnectorException.class)
    public void testStartTwice() throws BridgeConnectorException {

        Bridge localBridge = context.mock(Bridge.class);

        connector.start(localBridge);
        connector.start(localBridge);
    }

    @Test (expected = BridgeConnectorException.class)
    public void testStartConsumed() throws BridgeConnectorException {

        Bridge localBridge = context.mock(Bridge.class);

        connector.start(localBridge);
        connector.stop();
        connector.start(localBridge);
    }

    @Test
    public void testStop() throws BridgeConnectorException {

        Bridge localBridge = context.mock(Bridge.class);

        connector.start(localBridge);
        connector.stop();

        assertNull("Local bridge was not removed from the connector after stopping the connector", connector.getLocalBridge());
    }

    @Test (expected = BridgeConnectorException.class)
    public void testStopNotUsed() throws BridgeConnectorException {

        connector.stop();
    }

    private static class TestAbstractBridgeConnector extends AbstractBridgeConnector {

        @Override
        public Bridge getLocalBridge() {

            return super.getLocalBridge();
        }

        @Override
        public void send(Event event) {

            // Empty
        }

    }

}
