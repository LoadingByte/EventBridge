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

package com.quartercode.eventbridge.test.basic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import com.quartercode.eventbridge.basic.AbstractBridgeModule;
import com.quartercode.eventbridge.bridge.Bridge;

public class AbstractBridgeModuleTest {

    @Rule
    public JUnitRuleMockery          context = new JUnitRuleMockery();

    private TestAbstractBridgeModule module;

    @Before
    public void setUp() {

        module = new TestAbstractBridgeModule();
    }

    @Test
    public void testAdd() {

        Bridge bridge = context.mock(Bridge.class);

        module.add(bridge);

        assertEquals("Local bridge that is stored by the connector", bridge, module.getBridge());
    }

    @Test (expected = IllegalStateException.class)
    public void testAddTwice() {

        Bridge bridge = context.mock(Bridge.class);

        module.add(bridge);
        module.add(bridge);
    }

    @Test (expected = IllegalStateException.class)
    public void testAddConsumed() {

        Bridge bridge = context.mock(Bridge.class);

        module.add(bridge);
        module.remove();
        module.add(bridge);
    }

    @Test
    public void testStop() {

        Bridge bridge = context.mock(Bridge.class);

        module.add(bridge);
        module.remove();

        assertNull("Local bridge was not removed from the connector after stopping the connector", module.getBridge());
    }

    @Test (expected = IllegalStateException.class)
    public void testStopNotUsed() {

        module.remove();
    }

    private static class TestAbstractBridgeModule extends AbstractBridgeModule {

    }

}
