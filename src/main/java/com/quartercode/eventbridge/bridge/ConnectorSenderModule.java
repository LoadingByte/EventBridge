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
 * The {@link BridgeModule} which takes care of delivering {@link Event}s to all {@link BridgeConnector}s of the module's {@link Bridge}.
 * It hooks into the {@link SenderModule}'s channel for diverting sent events into its global connector send channel ({@link #getGlobalChannel()}).
 * The global channel the splits the invocation into multiple ones for every bridge connector.
 * These specific invocations are transported through the specific connector send channel ({@link #getSpecificChannel()}).
 * 
 * @see Event
 * @see BridgeConnector
 */
public interface ConnectorSenderModule extends BridgeModule {

    /**
     * Returns the {@link Channel} which delivers {@link Event}s to the specific connector send channel ({@link #getSpecificChannel()}).
     * The last interceptor splits an invocation of the channel into multiple ones for every {@link BridgeConnector}.
     * The channel is invoked by the {@link #send(Event)} method.
     * 
     * @return The channel which delivers events to the specific connector send channel.
     */
    public Channel<GlobalConnectorSendInterceptor> getGlobalChannel();

    /**
     * Returns the {@link Channel} which delivers {@link Event}s to a specific {@link BridgeConnector}.
     * It is invoked by the last interceptor of the global connector send channel ({@link #getGlobalChannel()}).
     * 
     * @return The channel which delivers events to a specific bridge connector.
     */
    public Channel<SpecificConnectorSendInterceptor> getSpecificChannel();

    /**
     * Sends the given {@link Event} through the global connector send channel ({@link #getGlobalChannel()}).
     * 
     * @param event The event which should be sent through the global connector send channel.
     */
    public void send(Event event);

    /**
     * The interceptor which is used in the global connector send channel of a {@link ConnectorSenderModule}.
     * 
     * @see ConnectorSenderModule#getGlobalChannel()
     */
    public static interface GlobalConnectorSendInterceptor {

        /**
         * Intercepts the delivery process of the given {@link Event} to the specific connector send channel of the {@link ConnectorSenderModule}.
         * 
         * @param invocation The {@link ChannelInvocation} object for the current invocation chain.
         * @param event The event which is transported through the channel.
         *        It should be delivered to the specific connector send channel.
         */
        public void send(ChannelInvocation<GlobalConnectorSendInterceptor> invocation, Event event);

    }

    /**
     * The interceptor which is used in the specific connector send channel of a {@link ConnectorSenderModule}.
     * 
     * @see ConnectorSenderModule#getSpecificChannel()
     */
    public static interface SpecificConnectorSendInterceptor {

        /**
         * Intercepts the delivery process of the given {@link Event} to the given {@link BridgeConnector}.
         * 
         * @param invocation The {@link ChannelInvocation} object for the current invocation chain.
         * @param event The event which is transported through the channel.
         *        It should be sent through the given bridge connector.
         * @param connector The bridge connector through which the given event should be sent.
         */
        public void send(ChannelInvocation<SpecificConnectorSendInterceptor> invocation, Event event, BridgeConnector connector);

    }

}
