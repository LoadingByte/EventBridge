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

package com.quartercode.eventbridge.extra.extension;

import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.BridgeModule;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.module.EventHandler;

/**
 * The send predicate check extension prevents all {@link Event}s, which the receiver {@link Bridge} does hot have any {@link EventHandler} for, from being sent.
 * It can remove a lot of load from {@link BridgeConnector}s if many events are sent but not received.<br>
 * <br>
 * Since the send predicate check extension is a {@link BridgeModule}, it can be added to a bridge as follows:
 * 
 * <pre>
 * Bridge bridge = ...
 * SendPredicateCheckExtension extension = ...
 * bridge.addModule(extension);
 * </pre>
 * 
 * Please note that the extension also can be removed from a bridge:
 * 
 * <pre>
 * Bridge bridge = ...
 * SendPredicateCheckExtension extension = ...
 * bridge.addModule(extension);
 * ...
 * bridge.removeModule(bridge.getModule(SendPredicateCheckExtension.class));
 * </pre>
 * 
 * @see BridgeModule
 * @see Bridge
 * @see BridgeConnector
 */
public interface SendPredicateCheckExtension extends BridgeModule {

}
