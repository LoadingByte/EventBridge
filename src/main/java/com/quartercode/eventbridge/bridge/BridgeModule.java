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

package com.quartercode.eventbridge.bridge;

/**
 * Bridge modules are classes which store bridge-related objects and provide bridge functionality outside the main bridge class.
 * They can be retrieved with the {@link Bridge#getModule(Class)} method.
 * 
 * @see Bridge
 */
public interface BridgeModule {

    /**
     * Returns the parent {@link Bridge} the bridge module was added to.
     * May be {@code null} if the module hasn't yet been added to a bridge or was already removed from a bridge.
     * 
     * @return The bridge the module is added to.
     */
    public Bridge getBridge();

    /**
     * This method is invoked when the bridge module is added to the given {@link Bridge}.
     * It can only be called once.
     * 
     * @param bridge The bridge the module is added to.
     */
    public void add(Bridge bridge);

    /**
     * This method is invoked when the bridge module is removed from the {@link Bridge} it was added to.
     * It can only be called once after the {@link #add(Bridge)} method has been invoked.
     */
    public void remove();

}
