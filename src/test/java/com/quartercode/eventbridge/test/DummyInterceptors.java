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

package com.quartercode.eventbridge.test;

import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.EventHandler;
import com.quartercode.eventbridge.bridge.HandlerModule.GlobalHandleInterceptor;
import com.quartercode.eventbridge.bridge.HandlerModule.HandlerHandleInterceptor;
import com.quartercode.eventbridge.bridge.SenderModule.ConnectorSendInterceptor;
import com.quartercode.eventbridge.bridge.SenderModule.GlobalSendInterceptor;
import com.quartercode.eventbridge.bridge.SenderModule.LocalHandlerSendInterceptor;
import com.quartercode.eventbridge.channel.ChannelInvocation;

public class DummyInterceptors {

    public static class DummyGlobalHandleInterceptor implements GlobalHandleInterceptor {

        private final GlobalHandleInterceptor dummy;

        public DummyGlobalHandleInterceptor(GlobalHandleInterceptor dummy) {

            this.dummy = dummy;
        }

        @Override
        public void handle(ChannelInvocation<GlobalHandleInterceptor> invocation, BridgeConnector source, Event event) {

            dummy.handle(invocation, source, event);
            invocation.next().handle(invocation, source, event);
        }

    }

    public static class DummyHandlerHandleInterceptor implements HandlerHandleInterceptor {

        private final HandlerHandleInterceptor dummy;

        public DummyHandlerHandleInterceptor(HandlerHandleInterceptor dummy) {

            this.dummy = dummy;
        }

        @Override
        public void handle(ChannelInvocation<HandlerHandleInterceptor> invocation, BridgeConnector source, EventHandler<?> handler, Event event) {

            dummy.handle(invocation, source, handler, event);
            invocation.next().handle(invocation, source, handler, event);
        }

    }

    public static class DummyGlobalSendInterceptor implements GlobalSendInterceptor {

        private final GlobalSendInterceptor dummy;

        public DummyGlobalSendInterceptor(GlobalSendInterceptor dummy) {

            this.dummy = dummy;
        }

        @Override
        public void send(ChannelInvocation<GlobalSendInterceptor> invocation, Event event) {

            dummy.send(invocation, event);
            invocation.next().send(invocation, event);
        }

    }

    public static class DummyLocalHandlerSendInterceptor implements LocalHandlerSendInterceptor {

        private final LocalHandlerSendInterceptor dummy;

        public DummyLocalHandlerSendInterceptor(LocalHandlerSendInterceptor dummy) {

            this.dummy = dummy;
        }

        @Override
        public void send(ChannelInvocation<LocalHandlerSendInterceptor> invocation, Event event) {

            dummy.send(invocation, event);
            invocation.next().send(invocation, event);
        }

    }

    public static class DummyConnectorSendInterceptor implements ConnectorSendInterceptor {

        private final ConnectorSendInterceptor dummy;

        public DummyConnectorSendInterceptor(ConnectorSendInterceptor dummy) {

            this.dummy = dummy;
        }

        @Override
        public void send(ChannelInvocation<ConnectorSendInterceptor> invocation, BridgeConnector connector, Event event) {

            dummy.send(invocation, connector, event);
            invocation.next().send(invocation, connector, event);
        }

    }

    private DummyInterceptors() {

    }

}
