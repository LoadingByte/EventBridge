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

package com.quartercode.eventbridge.basic;

import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.BridgeModule;

/**
 * Bridge module base is the base class for all modules of a {@link Bridge}.
 * Basically, bridge modules are just classes which store bridge functionality outside the main bridge class.
 * 
 * @see BridgeModule
 */
public abstract class AbstractBridgeModule implements BridgeModule {

    private Bridge  bridge;
    private boolean consumed;

    @Override
    public Bridge getBridge() {

        return bridge;
    }

    @Override
    public void add(Bridge bridge) {

        if (consumed) {
            throw new IllegalStateException("Bridge module was already consumed (added and removed)");
        } else if (this.bridge != null) {
            throw new IllegalStateException("Can't add bridge module: Already used by another bridge");
        }

        this.bridge = bridge;
    }

    @Override
    public void remove() {

        if (bridge == null) {
            throw new IllegalStateException("Can't remove bridge module: Not used by any bridge");
        }

        bridge = null;
        consumed = true;
    }

}
