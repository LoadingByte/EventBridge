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

package com.quartercode.eventbridge.def.bridge.connector;

import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.BridgeConnectorException;

/**
 * The abstract bridge connector class hides some boilerplate code that every {@link BridgeConnector} must implement.
 * For example, it checks whether the {@link #start(Bridge)} and {@link #stop()} methods may be called and
 * throws exceptions if a call is not allowed.
 * 
 * @see BridgeConnector
 */
public abstract class AbstractBridgeConnector implements BridgeConnector {

    private Bridge  localBridge;
    private boolean consumed;

    /**
     * Creates a new abstract bridge connector.
     */
    public AbstractBridgeConnector() {

    }

    /**
     * Returns the local {@link Bridge} which this bridge connector connects with some second bridge.
     * The return value of this method is set during the {@link #start(Bridge)} call.
     * 
     * @return The local bridge of the bridge connector.
     */
    protected Bridge getLocalBridge() {

        return localBridge;
    }

    @Override
    public void start(Bridge localBridge) throws BridgeConnectorException {

        if (consumed) {
            throw new BridgeConnectorException(this, new IllegalStateException("Bridge connector was already consumed (started and stopped)"));
        } else if (this.localBridge != null) {
            throw new BridgeConnectorException(this, new IllegalStateException("Can't start bridge connector: Already used by another bridge"));
        }

        this.localBridge = localBridge;
    }

    @Override
    public void stop() throws BridgeConnectorException {

        if (localBridge == null) {
            throw new BridgeConnectorException(this, new IllegalStateException("Can't stop bridge connector: Not used by any bridge"));
        }

        localBridge = null;
        consumed = true;
    }

}
