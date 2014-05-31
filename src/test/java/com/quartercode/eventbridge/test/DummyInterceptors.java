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
import com.quartercode.eventbridge.bridge.module.ConnectorSenderModule.GlobalConnectorSendInterceptor;
import com.quartercode.eventbridge.bridge.module.ConnectorSenderModule.SpecificConnectorSendInterceptor;
import com.quartercode.eventbridge.bridge.module.HandlerModule.GlobalHandleInterceptor;
import com.quartercode.eventbridge.bridge.module.HandlerModule.HandlerHandleInterceptor;
import com.quartercode.eventbridge.bridge.module.LocalHandlerSenderModule.LocalHandlerSendInterceptor;
import com.quartercode.eventbridge.bridge.module.SenderModule.SendInterceptor;
import com.quartercode.eventbridge.channel.ChannelInvocation;

public class DummyInterceptors {

    public static class DummySendInterceptor implements SendInterceptor {

        private final SendInterceptor dummy;

        public DummySendInterceptor(SendInterceptor dummy) {

            this.dummy = dummy;
        }

        @Override
        public void send(ChannelInvocation<SendInterceptor> invocation, Event event) {

            dummy.send(invocation, event);
            invocation.next().send(invocation, event);
        }

    }

    public static class DummyGlobalConnectorSendInterceptor implements GlobalConnectorSendInterceptor {

        private final GlobalConnectorSendInterceptor dummy;

        public DummyGlobalConnectorSendInterceptor(GlobalConnectorSendInterceptor dummy) {

            this.dummy = dummy;
        }

        @Override
        public void send(ChannelInvocation<GlobalConnectorSendInterceptor> invocation, Event event) {

            dummy.send(invocation, event);
            invocation.next().send(invocation, event);
        }

    }

    public static class DummySpecificConnectorSendInterceptor implements SpecificConnectorSendInterceptor {

        private final SpecificConnectorSendInterceptor dummy;

        public DummySpecificConnectorSendInterceptor(SpecificConnectorSendInterceptor dummy) {

            this.dummy = dummy;
        }

        @Override
        public void send(ChannelInvocation<SpecificConnectorSendInterceptor> invocation, Event event, BridgeConnector connector) {

            dummy.send(invocation, event, connector);
            invocation.next().send(invocation, event, connector);
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

    private DummyInterceptors() {

    }

}
