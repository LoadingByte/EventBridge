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

package com.quartercode.eventbridge.test;

import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.module.ConnectorSenderModule.GlobalConnectorSendInterceptor;
import com.quartercode.eventbridge.bridge.module.ConnectorSenderModule.SpecificConnectorSendInterceptor;
import com.quartercode.eventbridge.bridge.module.EventHandler;
import com.quartercode.eventbridge.bridge.module.HandlerModule.HandleInterceptor;
import com.quartercode.eventbridge.bridge.module.LocalHandlerSenderModule.LocalHandlerSendInterceptor;
import com.quartercode.eventbridge.bridge.module.LowLevelHandler;
import com.quartercode.eventbridge.bridge.module.LowLevelHandlerModule.GlobalLowLevelHandleInterceptor;
import com.quartercode.eventbridge.bridge.module.LowLevelHandlerModule.SpecificLowLevelHandleInterceptor;
import com.quartercode.eventbridge.bridge.module.SenderModule.SendInterceptor;
import com.quartercode.eventbridge.bridge.module.StandardHandlerModule.StandardHandleExceptionInterceptor;
import com.quartercode.eventbridge.bridge.module.StandardHandlerModule.StandardHandleInterceptor;
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

    public static class DummyHandleInterceptor implements HandleInterceptor {

        private final HandleInterceptor dummy;

        public DummyHandleInterceptor(HandleInterceptor dummy) {

            this.dummy = dummy;
        }

        @Override
        public void handle(ChannelInvocation<HandleInterceptor> invocation, Event event, BridgeConnector source) {

            dummy.handle(invocation, event, source);
            invocation.next().handle(invocation, event, source);
        }

    }

    public static class DummyGlobalLowLevelHandleInterceptor implements GlobalLowLevelHandleInterceptor {

        private final GlobalLowLevelHandleInterceptor dummy;

        public DummyGlobalLowLevelHandleInterceptor(GlobalLowLevelHandleInterceptor dummy) {

            this.dummy = dummy;
        }

        @Override
        public void handle(ChannelInvocation<GlobalLowLevelHandleInterceptor> invocation, Event event, BridgeConnector source) {

            dummy.handle(invocation, event, source);
            invocation.next().handle(invocation, event, source);
        }

    }

    public static class DummySpecificLowLevelHandleInterceptor implements SpecificLowLevelHandleInterceptor {

        private final SpecificLowLevelHandleInterceptor dummy;

        public DummySpecificLowLevelHandleInterceptor(SpecificLowLevelHandleInterceptor dummy) {

            this.dummy = dummy;
        }

        @Override
        public void handle(ChannelInvocation<SpecificLowLevelHandleInterceptor> invocation, Event event, BridgeConnector source, LowLevelHandler handler) {

            dummy.handle(invocation, event, source, handler);
            invocation.next().handle(invocation, event, source, handler);
        }

    }

    public static class DummyStandardHandleInterceptor implements StandardHandleInterceptor {

        private final StandardHandleInterceptor dummy;

        public DummyStandardHandleInterceptor(StandardHandleInterceptor dummy) {

            this.dummy = dummy;
        }

        @Override
        public void handle(ChannelInvocation<StandardHandleInterceptor> invocation, Event event, BridgeConnector source, EventHandler<?> handler) {

            dummy.handle(invocation, event, source, handler);
            invocation.next().handle(invocation, event, source, handler);
        }

    }

    public static class DummyStandardHandleExceptionInterceptor implements StandardHandleExceptionInterceptor {

        private final StandardHandleExceptionInterceptor dummy;

        public DummyStandardHandleExceptionInterceptor(StandardHandleExceptionInterceptor dummy) {

            this.dummy = dummy;
        }

        @Override
        public void handle(ChannelInvocation<StandardHandleExceptionInterceptor> invocation, RuntimeException exception, EventHandler<?> handler, Event event, BridgeConnector source) {

            dummy.handle(invocation, exception, handler, event, source);
            invocation.next().handle(invocation, exception, handler, event, source);
        }

    }

    private DummyInterceptors() {

    }

}
