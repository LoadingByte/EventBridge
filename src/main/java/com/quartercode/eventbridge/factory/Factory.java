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

package com.quartercode.eventbridge.factory;

/**
 * A factory is a simple class which just creates objects of a certain type.
 * All created objects must be exactly equal to each other, but they may not be the same.
 */
public interface Factory {

    /**
     * Creates a new object. The type of that object is defined by the factory class.
     * When this method is called multiple times, all returned objects must be exactly equal to each other, but they may not be the same.
     * 
     * @return A newly created object.
     */
    public Object create();

}
