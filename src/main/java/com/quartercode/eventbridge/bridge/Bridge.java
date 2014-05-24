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

/**
 * Bridges allow to connect two parts of an application that must not run on the same vm or machine.
 * The connection between bridges can be changed without much effort.
 * Generally, bridges communicate with {@link Event}s that are sent between the bridges.<br>
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
     * Returns the {@link SenderModule} which is responsible for sending {@link Event}s between bridges.
     * 
     * @return The bridge's sender module.
     */
    public SenderModule getSenderModule();

    /**
     * Lets the given {@link Event} be sent by the bridge's sender module ({@link #getSenderModule()}).
     * This method is basically a shortcut for the {@link SenderModule#send(Event)} method.
     * 
     * @param event The event that should be sent by the sender module.
     */
    public void send(Event event);

    // ----- Storage -----

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
