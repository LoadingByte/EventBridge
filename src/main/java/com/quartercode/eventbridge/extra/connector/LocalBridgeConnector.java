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

package com.quartercode.eventbridge.extra.connector;

import com.quartercode.eventbridge.basic.AbstractBridgeConnector;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.BridgeConnectorException;
import com.quartercode.eventbridge.bridge.Event;

/**
 * The local bridge connector is a simple {@link BridgeConnector} that connects two {@link Bridge}s that run on the same vm.
 * 
 * @see BridgeConnector
 */
public class LocalBridgeConnector extends AbstractBridgeConnector {

    private Bridge               remoteBridge;

    private LocalBridgeConnector remoteConnector;

    /**
     * Creates a new local bridge connector that connects to the given remote {@link Bridge}.
     * 
     * @param remoteBridge The second {@link Bridge} the local bridge connector connects to.
     */
    public LocalBridgeConnector(Bridge remoteBridge) {

        this.remoteBridge = remoteBridge;
    }

    private LocalBridgeConnector(LocalBridgeConnector remoteConnector) {

        this.remoteConnector = remoteConnector;
    }

    @Override
    public void start(Bridge localBridge) throws BridgeConnectorException {

        super.start(localBridge);

        // Connect the remote bridge to the local bridge (add reverse connection)
        if (remoteBridge != null) {
            remoteConnector = new LocalBridgeConnector(this);
            remoteBridge.addConnector(remoteConnector);
        }
    }

    @Override
    public void stop() throws BridgeConnectorException {

        super.stop();

        // Disconnect the remote bridge from the local bridge (remove reverse connection)
        remoteConnector.disconnect();

        remoteBridge = null;
        remoteConnector = null;
    }

    @Override
    public void send(Event event) {

        remoteConnector.handle(event);
    }

    private void handle(Event event) {

        getLocalBridge().handle(this, event);
    }

    private void disconnect() throws BridgeConnectorException {

        if (!isStopped()) {
            getLocalBridge().removeConnector(this);
        }
    }

    private boolean isStopped() {

        return getLocalBridge() == null;
    }

}
