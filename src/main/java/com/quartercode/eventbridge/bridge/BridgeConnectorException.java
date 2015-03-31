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

package com.quartercode.eventbridge.bridge;

/**
 * The bridge connector exception is a checked exception that can occur when a {@link BridgeConnector} produces an error.
 * The error can be caused by both the user of a connector or the connector itself.
 * 
 * @see BridgeConnector
 */
public class BridgeConnectorException extends Exception {

    private static final long     serialVersionUID = -2274106332875418326L;

    private final BridgeConnector connector;

    /**
     * Creates a new (nearly) empty bridge connector exception.
     * 
     * @param connector The {@link BridgeConnector} that threw or caused the exception.
     */
    public BridgeConnectorException(BridgeConnector connector) {

        this.connector = connector;
    }

    /**
     * Creates a new bridge connector exception with a detail message which describes the error.
     * 
     * @param connector The {@link BridgeConnector} that threw or caused the exception.
     * @param message A detail message which describes the error that occurred.
     */
    public BridgeConnectorException(BridgeConnector connector, String message) {

        super(message);

        this.connector = connector;
    }

    /**
     * Creates a new bridge connector exception with the exception which caused the error.
     * 
     * @param connector The {@link BridgeConnector} that threw or caused the exception.
     * @param cause The cause exception which caused the error in the first place.
     */
    public BridgeConnectorException(BridgeConnector connector, Throwable cause) {

        super(cause);

        this.connector = connector;
    }

    /**
     * Creates a new bridge connector exception with a detail message which describes the error and the exception that caused it.
     * 
     * @param connector The {@link BridgeConnector} that threw or caused the exception.
     * @param message A detail message which describes the error that occurred.
     * @param cause The cause exception which caused the error in the first place.
     */
    public BridgeConnectorException(BridgeConnector connector, String message, Throwable cause) {

        super(message, cause);

        this.connector = connector;
    }

    /**
     * Returns the {@link BridgeConnector} that threw or caused the connector exception.
     * 
     * @return The connector that caused the exception.
     */
    public BridgeConnector getConnector() {

        return connector;
    }

}
