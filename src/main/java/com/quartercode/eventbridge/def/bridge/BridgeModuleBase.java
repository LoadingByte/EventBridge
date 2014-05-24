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

package com.quartercode.eventbridge.def.bridge;

import com.quartercode.eventbridge.bridge.Bridge;

/**
 * Bridge module base is the base class for all modules of a {@link Bridge}.
 * Basically, bridge modules are just classes which store bridge functionality outside the main bridge class.
 */
class BridgeModuleBase {

    private final Bridge parent;

    /**
     * Creates a new bridge module base object.
     * 
     * @param parent The parent {@link Bridge} that uses the module.
     */
    protected BridgeModuleBase(Bridge parent) {

        this.parent = parent;
    }

    /**
     * Returns the parent {@link Bridge} that uses the module object.
     * 
     * @return The bridge that uses the module.
     */
    protected Bridge getParent() {

        return parent;
    }

}
