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

import com.quartercode.eventbridge.channel.Channel;
import com.quartercode.eventbridge.channel.ChannelInvocation;

/**
 * The part of a {@link Bridge} which takes care of transporting {@link Event}s to all connected bridges.
 * 
 * @see Bridge
 * @see Event
 */
public interface SenderModule {

    /**
     * Returns the {@link Channel} which delivers {@link Event}s to all local handlers and the connector send channel of the module.
     * It is invoked by the {@link #send(Event)} method.
     * The connector send channel can be accessed with {@link #getConnectorSendChannel()}.
     * 
     * @return The channel which delivers events to the connector send channel.
     */
    public Channel<GlobalSendInterceptor> getGlobalSendChannel();

    /**
     * Returns the {@link Channel} which delivers {@link Event}s to all interested {@link BridgeConnector}s.
     * It is invoked by the last interceptor of the global send channel ({@link #getGlobalSendChannel()}).
     * 
     * @return The channel which delivers events to the actual bridge connectors.
     */
    public Channel<ConnectorSendInterceptor> getConnectorSendChannel();

    /**
     * Sends the given {@link Event} through the global send channel ({@link #getGlobalSendChannel()}).
     * 
     * @param event The event that should be sent through the global send channel.
     */
    public void send(Event event);

    /**
     * The interceptor which is used in the global send channel of a {@link SenderModule}.
     * 
     * @see SenderModule#getGlobalSendChannel()
     */
    public static interface GlobalSendInterceptor {

        /**
         * Intercepts the delivery process of the given {@link Event} to the connector send channel ({@link SenderModule#getConnectorSendChannel()}).
         * 
         * @param invocation The {@link ChannelInvocation} object for the current invocation chain.
         * @param event The event which is delivered to the connector send channel.
         */
        public void send(ChannelInvocation<GlobalSendInterceptor> invocation, Event event);

    }

    /**
     * The interceptor which is used in the connector send channel of a {@link SenderModule}.
     * 
     * @see SenderModule#getConnectorSendChannel()
     */
    public static interface ConnectorSendInterceptor {

        /**
         * Intercepts the delivery process of the given {@link Event} to the given {@link BridgeConnector}.
         * 
         * @param invocation The {@link ChannelInvocation} object for the current invocation chain.
         * @param connector The bridge connector through which the given event should be sent.
         * @param event The event which should be sent through the given bridge connector.
         */
        public void send(ChannelInvocation<ConnectorSendInterceptor> invocation, BridgeConnector connector, Event event);

    }

}
