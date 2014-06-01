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

package com.quartercode.eventbridge;

import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.module.ConnectorSenderModule;
import com.quartercode.eventbridge.bridge.module.HandlerModule;
import com.quartercode.eventbridge.bridge.module.LocalHandlerSenderModule;
import com.quartercode.eventbridge.bridge.module.LowLevelHandlerModule;
import com.quartercode.eventbridge.bridge.module.SenderModule;
import com.quartercode.eventbridge.bridge.module.StandardHandlerModule;
import com.quartercode.eventbridge.extra.extension.ReturnEventExtensionRequester;
import com.quartercode.eventbridge.extra.extension.ReturnEventExtensionReturner;
import com.quartercode.eventbridge.extra.extension.SendPredicateCheckExtension;
import com.quartercode.eventbridge.factory.FactoryManager;

public class EventBridgeFactory {

    private static FactoryManager factoryManager = new FactoryManager();

    static {

        factoryManager.setFactory(Bridge.class, new DefaultBridgeFactory());

        factoryManager.setFactory(SenderModule.class, new DefaultSenderModuleFactory());
        factoryManager.setFactory(ConnectorSenderModule.class, new DefaultConnectorSenderModuleFactory());
        factoryManager.setFactory(LocalHandlerSenderModule.class, new DefaultLocalHandlerSenderModuleFactory());
        factoryManager.setFactory(HandlerModule.class, new DefaultHandlerModuleFactory());
        factoryManager.setFactory(LowLevelHandlerModule.class, new DefaultLowLevelHandlerModuleFactory());
        factoryManager.setFactory(StandardHandlerModule.class, new DefaultStandardHandlerModuleFactory());

        factoryManager.setFactory(SendPredicateCheckExtension.class, new DefaultSendPredicateCheckExtensionFactory());
        factoryManager.setFactory(ReturnEventExtensionRequester.class, new DefaultReturnEventExtensionRequesterFactory());
        factoryManager.setFactory(ReturnEventExtensionReturner.class, new DefaultReturnEventExtensionReturnerFactory());

    }

    public static FactoryManager getFactoryManager() {

        return factoryManager;
    }

    public static <T> T create(Class<T> type) {

        return factoryManager.create(type);
    }

    private EventBridgeFactory() {

    }

}
