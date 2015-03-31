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

package com.quartercode.eventbridge.factory;

import java.util.HashMap;
import java.util.Map;

/**
 * A factory manager assigns {@link Factory} instances to abstract types.
 * For example, a factory for objects of the type {@code B implements A} could be assigned to the interface type {@code A}.
 * Users can simply input {@code A} in the {@link #create(Class)} method and get a new instance of {@code B} without knowing that it even exists.
 * 
 * @see Factory
 */
public class FactoryManager {

    private final Map<Class<?>, Factory> factories = new HashMap<>();

    /**
     * Assigns the given {@link Factory} to the given abstract type.
     * The type must not be abstract, but it's recommended that only interfaces are added here.
     * The given factory then creates objects when the {@link #create(Class)} method is called with the given type.
     * 
     * @param type The abstract type the given factory is assigned to.
     * @param factory The factory which is assigned to the given abstract type.
     */
    public void setFactory(Class<?> type, Factory factory) {

        factories.put(type, factory);
    }

    /**
     * Creates a new object of the given abstract type.
     * Internally, a {@link Factory}, which was mapped with {@link #setFactory(Class, Factory)}, creates the object.
     * 
     * @param type The type of the object that should be created.
     * @return The newly created object.
     * @throws IllegalArgumentException There is no factory mapped for the given type.
     */
    public <T> T create(Class<T> type) {

        if (!factories.containsKey(type)) {
            throw new IllegalArgumentException("Factory manager doesn't contain factory for type '" + type + "'");
        }

        Factory factory = factories.get(type);
        return type.cast(factory.create());
    }

}
