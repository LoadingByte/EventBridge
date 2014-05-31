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

import java.util.List;
import com.quartercode.eventbridge.bridge.module.HandlerModule;
import com.quartercode.eventbridge.bridge.module.SenderModule;

/**
 * Bridges allow to connect two parts of an application that must not run on the same vm or machine.
 * The connection between bridges can be changed without much effort.
 * Generally, bridges communicate with {@link Event}s that are sent between the bridges.
 * Events are sent by the first bridge, received by the second bridge and finally processed by some {@link EventHandler}s.
 * However, events can also be sent to one bridge which then hands them over to its local handlers.<br>
 * <br>
 * {@link BridgeConnector}s are used for connecting a bridge with some other bridges.
 * The {@link #addConnector(BridgeConnector)} method activates a connector and binds it,
 * while the {@link #removeConnector(BridgeConnector)} method does the opposite.
 * The usage of bridge connectors inverts the dependency on details and decouples the bridge from the event transport logic.<br>
 * <br>
 * Bridge modules are used to seperate the bridge concerns.
 * They are generally provided by the bridge implementation and take care of a specific aspect of bridges.
 * For example, a bridge module could be used to send events to other bridges, while a second module could handle incoming events.
 * 
 * @see Event
 * @see EventHandler
 * @see BridgeConnector
 * @see SenderModule
 */
public interface Bridge {

    // ----- Modules -----

    /**
     * Returns the {@link BridgeModule} which is an instance of the given type.
     * If multiple modules are instances of the given type, the first one is returned.
     * If no module is an instance of the given type, {@code null} is returned.
     * 
     * @param type The type of the returned module (cannot be {@code null}).
     * @return The first module which is assignable to the given type.
     */
    public <T extends BridgeModule> T getModule(Class<T> type);

    /**
     * Adds the given {@link BridgeModule} to the bridge and calls its {@link BridgeModule#add(Bridge)} method.
     * If a module which has the same type as the new one has already been added, an {@link IllegalArgumentException} is thrown.
     * 
     * @param module The module to add to the bridge (cannot be {@code null}).
     * @throws IllegalArgumentException A module which has the same type as the new one has already been added.
     */
    public void addModule(BridgeModule module);

    /**
     * Removes the given {@link BridgeModule} from the bridge and calls its {@link BridgeModule#remove()} method.
     * 
     * @param module The module to remove from the bridge (cannot be {@code null}).
     */
    public void removeModule(BridgeModule module);

    // ----- Shortcuts -----

    /**
     * Passes the given {@link Event} to the bridge's {@link SenderModule}.
     * This method is basically a shortcut for the {@link SenderModule#send(Event)} method.
     * 
     * @param event The event that should be passed to the sender module.
     */
    public void send(Event event);

    /**
     * Passes the given {@link Event} to the bridge's {@link HandlerModule}.
     * This method is basically a shortcut for the {@link HandlerModule#handle(BridgeConnector, Event)} method.
     * 
     * @param source The {@link BridgeConnector} which received the event.
     *        May be {@code null} if the handled event was sent from the same bridge which is handling it.
     * @param event The event that should be passed to the handler module.
     */
    public void handle(BridgeConnector source, Event event);

    // ----- Connectors -----

    /**
     * Returns an unmodifiable list that contains all active and bound {@link BridgeConnector}s which are connected to other bridges.
     * 
     * @return All bound and connected bridge connectors.
     */
    public List<BridgeConnector> getConnectors();

    /**
     * Starts up the given {@link BridgeConnector} and connects this bridge with another bridge which is defined by the connector.
     * 
     * @param connector The bridge connector that should connect this bride with another bridge.
     * @throws BridgeConnectorException Something goes wrong while starting up the connector.
     */
    public void addConnector(BridgeConnector connector) throws BridgeConnectorException;

    /**
     * Shuts down the given {@link BridgeConnector} and disconnects this bridge from the bridge the connector connected it to.
     * Please note that the connector on the other side must call this method on its bridge with itself once the connection is destroyed.
     * 
     * @param connector The bridge connector that should disconnect this bride from another bridge.
     * @throws BridgeConnectorException Something goes wrong while shutting down the connector.
     */
    public void removeConnector(BridgeConnector connector) throws BridgeConnectorException;

    /**
     * Adds the given {@link ModifyConnectorListListener} that is called when a {@link BridgeConnector} is added or removed.
     * 
     * @param listener The listener that should be added.
     * @see #addConnector(BridgeConnector)
     * @see #removeConnector(BridgeConnector)
     */
    public void addModifyConnectorListListener(ModifyConnectorListListener listener);

    /**
     * Removes the given {@link ModifyConnectorListListener} that is called when a {@link BridgeConnector} is added or removed.
     * 
     * @param listener The listener that should be removed.
     * @see #addConnector(BridgeConnector)
     * @see #removeConnector(BridgeConnector)
     */
    public void removeModifyConnectorListListener(ModifyConnectorListListener listener);

    /**
     * A modify connector list listener is called when a {@link BridgeConnector} is added to or removed from a {@link Bridge}.
     */
    public static interface ModifyConnectorListListener {

        /**
         * This method is invoked when the given {@link BridgeConnector} is being added to the given {@link Bridge}.
         * It is called after the connector is added and started.
         * 
         * @param connector The bridge connector that is added to the bridge.
         * @param bridge The bridge the given connector is added to.
         */
        public void onAddConnector(BridgeConnector connector, Bridge bridge);

        /**
         * This method is invoked when the given {@link BridgeConnector} is being removed from the given {@link Bridge}.
         * It is called before the connector is removed and stopped.
         * 
         * @param connector The bridge connector that is removed from the bridge.
         * @param bridge The bridge the given connector is removed from.
         */
        public void onRemoveConnector(BridgeConnector connector, Bridge bridge);

    }

}
