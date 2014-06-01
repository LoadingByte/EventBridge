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

package com.quartercode.eventbridge.test.factory;

import static org.junit.Assert.assertTrue;
import java.util.Objects;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import com.quartercode.eventbridge.factory.Factory;
import com.quartercode.eventbridge.factory.FactoryManager;

public class FactoryManagerTest {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    private FactoryManager  factoryManager;

    @Before
    public void setUp() {

        factoryManager = new FactoryManager();
    }

    @Test
    public void test() {

        final Factory factory = context.mock(Factory.class);
        final Implementation result = new Implementation();

        // @formatter:off
        context.checking(new Expectations() {{

            oneOf(factory).create();
                will(returnValue(result));

        }});
        // @formatter:on

        factoryManager.setFactory(Abstract.class, factory);

        Abstract actualResult = factoryManager.create(Abstract.class);
        assertTrue("Factory manager created correct object", Objects.equals(result, actualResult));
    }

    @Test (expected = IllegalArgumentException.class)
    public void testWithUnknownType() {

        // No factory is registered for type Abstract
        factoryManager.create(Abstract.class);
    }

    private static interface Abstract {

    }

    private static class Implementation implements Abstract {

    }

}
