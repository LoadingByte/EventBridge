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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.BridgeConnectorException;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.SenderModule;

/**
 * The default implementation of the {@link Bridge} interface.
 * 
 * @see Bridge
 */
public class DefaultBridge implements Bridge {

    private SenderModule                            senderModule                 = new DefaultSenderModule(this);

    private final List<BridgeConnector>             connectors                   = new ArrayList<>();

    // Listeners
    private final List<ModifyConnectorListListener> modifyConnectorListListeners = new ArrayList<>();

    // Cache
    private List<BridgeConnector>                   connectorsUnmodifiableCache;

    /**
     * Creates a new default bridge.
     */
    public DefaultBridge() {

    }

    // ----- Modules -----

    @Override
    public SenderModule getSenderModule() {

        return senderModule;
    }

    /**
     * Lets the bridge use another {@link SenderModule} which is responsible for sending {@link Event}s between bridges.<br>
     * <br>
     * <i>This method is highly implementation-dependent and should not be used in production!</i>
     * 
     * @param senderModule The new sender module for the bridge.
     */
    public void setSenderModule(SenderModule senderModule) {

        this.senderModule = senderModule;
    }

    @Override
    public void send(Event event) {

        senderModule.send(event);
    }

    // ----- Storage -----

    @Override
    public List<BridgeConnector> getConnectors() {

        if (connectorsUnmodifiableCache == null) {
            connectorsUnmodifiableCache = Collections.unmodifiableList(connectors);
        }

        return connectorsUnmodifiableCache;
    }

    @Override
    public void addConnector(BridgeConnector connector) throws BridgeConnectorException {

        connectors.add(connector);
        connectorsUnmodifiableCache = null;

        connector.start(this);

        for (ModifyConnectorListListener listener : modifyConnectorListListeners) {
            listener.onAddConnector(connector, this);
        }
    }

    @Override
    public void removeConnector(BridgeConnector connector) throws BridgeConnectorException {

        for (ModifyConnectorListListener listener : modifyConnectorListListeners) {
            listener.onRemoveConnector(connector, this);
        }

        connector.stop();

        connectors.remove(connector);
        connectorsUnmodifiableCache = null;
    }

    @Override
    public void addModifyConnectorListListener(ModifyConnectorListListener listener) {

        modifyConnectorListListeners.add(listener);
    }

    @Override
    public void removeModifyConnectorListListener(ModifyConnectorListListener listener) {

        modifyConnectorListListeners.remove(listener);
    }

}