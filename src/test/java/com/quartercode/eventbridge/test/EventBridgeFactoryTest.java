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

package com.quartercode.eventbridge.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.quartercode.eventbridge.EventBridgeFactory;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.module.ConnectorSenderModule;
import com.quartercode.eventbridge.bridge.module.HandlerModule;
import com.quartercode.eventbridge.bridge.module.LocalHandlerSenderModule;
import com.quartercode.eventbridge.bridge.module.LowLevelHandlerModule;
import com.quartercode.eventbridge.bridge.module.SenderModule;
import com.quartercode.eventbridge.bridge.module.StandardHandlerModule;
import com.quartercode.eventbridge.def.bridge.DefaultBridge;
import com.quartercode.eventbridge.def.bridge.module.DefaultConnectorSenderModule;
import com.quartercode.eventbridge.def.bridge.module.DefaultHandlerModule;
import com.quartercode.eventbridge.def.bridge.module.DefaultLocalHandlerSenderModule;
import com.quartercode.eventbridge.def.bridge.module.DefaultLowLevelHandlerModule;
import com.quartercode.eventbridge.def.bridge.module.DefaultSenderModule;
import com.quartercode.eventbridge.def.bridge.module.DefaultStandardHandlerModule;
import com.quartercode.eventbridge.def.extra.extension.DefaultReturnEventExtensionRequester;
import com.quartercode.eventbridge.def.extra.extension.DefaultReturnEventExtensionReturner;
import com.quartercode.eventbridge.def.extra.extension.DefaultSendPredicateCheckExtension;
import com.quartercode.eventbridge.extra.extension.ReturnEventExtensionRequester;
import com.quartercode.eventbridge.extra.extension.ReturnEventExtensionReturner;
import com.quartercode.eventbridge.extra.extension.SendPredicateCheckExtension;

@RunWith (Parameterized.class)
public class EventBridgeFactoryTest {

    @Parameters
    public static Collection<Object[]> data() {

        List<Object[]> data = new ArrayList<>();

        data.add(new Object[] { Bridge.class, DefaultBridge.class });

        data.add(new Object[] { SenderModule.class, DefaultSenderModule.class });
        data.add(new Object[] { ConnectorSenderModule.class, DefaultConnectorSenderModule.class });
        data.add(new Object[] { LocalHandlerSenderModule.class, DefaultLocalHandlerSenderModule.class });
        data.add(new Object[] { HandlerModule.class, DefaultHandlerModule.class });
        data.add(new Object[] { LowLevelHandlerModule.class, DefaultLowLevelHandlerModule.class });
        data.add(new Object[] { StandardHandlerModule.class, DefaultStandardHandlerModule.class });

        data.add(new Object[] { SendPredicateCheckExtension.class, DefaultSendPredicateCheckExtension.class });
        data.add(new Object[] { ReturnEventExtensionReturner.class, DefaultReturnEventExtensionReturner.class });
        data.add(new Object[] { ReturnEventExtensionRequester.class, DefaultReturnEventExtensionRequester.class });

        return data;
    }

    private final Class<?> type;
    private final Object   resultType;

    public EventBridgeFactoryTest(Class<?> type, Class<?> resultType) {

        this.type = type;
        this.resultType = resultType;
    }

    @Test
    public void test() {

        Object actualResult = EventBridgeFactory.create(type);
        assertNotNull("Event bridge factory created null object", actualResult);
        assertEquals("Type of the object which was created by the event bridge factory", resultType, actualResult.getClass());
    }

    @Test
    public void testWithInternalFactory() {

        Object actualResult = EventBridgeFactory.getFactoryManager().create(type);
        assertNotNull("Factory manager of the event bridge factory created null object", actualResult);
        assertEquals("Type of the object which was created by the factory manager of the  event bridge factory", resultType, actualResult.getClass());
    }

}
