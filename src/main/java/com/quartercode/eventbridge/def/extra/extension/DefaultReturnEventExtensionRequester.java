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

package com.quartercode.eventbridge.def.extra.extension;

import java.util.HashMap;
import java.util.Map;
import com.quartercode.eventbridge.basic.AbstractBridgeModule;
import com.quartercode.eventbridge.basic.EventUtils;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.module.EventHandler;
import com.quartercode.eventbridge.bridge.module.HandlerModule;
import com.quartercode.eventbridge.bridge.module.HandlerModule.HandleInterceptor;
import com.quartercode.eventbridge.channel.Channel;
import com.quartercode.eventbridge.channel.ChannelInvocation;
import com.quartercode.eventbridge.def.channel.DefaultChannel;
import com.quartercode.eventbridge.extra.extension.ReturnEventExtensionRequester;

/**
 * The default default implementation of the {@link ReturnEventExtensionRequester} interface.
 * 
 * @see ReturnEventExtensionRequester
 */
public class DefaultReturnEventExtensionRequester extends AbstractBridgeModule implements ReturnEventExtensionRequester {

    private final Channel<RequestSendInterceptor>  requestSendChannel           = new DefaultChannel<>(RequestSendInterceptor.class);
    private final Channel<ReturnHandleInterceptor> returnHandleChannel          = new DefaultChannel<>(ReturnHandleInterceptor.class);

    private final CatchReturnHandleInterceptor     catchReturnHandleInterceptor = new CatchReturnHandleInterceptor();

    private long                                   nextRequestId                = 0;
    private final Map<Long, EventHandler<?>>       returnHandlers               = new HashMap<>();

    /**
     * Creates a new return event extension requester.
     * See the {@link ReturnEventExtensionRequester} javadoc for more details on how to use the extension.
     */
    public DefaultReturnEventExtensionRequester() {

        requestSendChannel.addInterceptor(new LastRequestSendInterceptor(), 0);
        returnHandleChannel.addInterceptor(new LastReturnHandleInterceptor(), 0);
    }

    @Override
    public void add(Bridge bridge) {

        super.add(bridge);

        bridge.getModule(HandlerModule.class).getChannel().addInterceptor(catchReturnHandleInterceptor, 500);
    }

    @Override
    public void remove() {

        getBridge().getModule(HandlerModule.class).getChannel().removeInterceptor(catchReturnHandleInterceptor);

        super.remove();
    }

    @Override
    public Channel<RequestSendInterceptor> getRequestSendChannel() {

        return requestSendChannel;
    }

    @Override
    public Channel<ReturnHandleInterceptor> getReturnHandleChannel() {

        return returnHandleChannel;
    }

    @Override
    public void sendRequest(Event request, EventHandler<?> returnHandler) {

        ChannelInvocation<RequestSendInterceptor> invocation = requestSendChannel.invoke();
        invocation.next().sendRequest(invocation, request, returnHandler);
    }

    private class LastRequestSendInterceptor implements RequestSendInterceptor {

        @Override
        public void sendRequest(ChannelInvocation<RequestSendInterceptor> invocation, Event request, EventHandler<?> returnHandler) {

            long requestId = nextRequestId();
            returnHandlers.put(requestId, returnHandler);
            getBridge().send(new ReturnEventExtensionWrapper(request, requestId, true));
        }

        private long nextRequestId() {

            long requestId = nextRequestId;
            nextRequestId++;
            return requestId;
        }

    }

    private static class LastReturnHandleInterceptor implements ReturnHandleInterceptor {

        @Override
        public void handleReturn(ChannelInvocation<ReturnHandleInterceptor> invocation, Event returnEvent, BridgeConnector source, EventHandler<?> returnHandler) {

            EventUtils.tryHandle(returnHandler, returnEvent);

            invocation.next().handleReturn(invocation, returnEvent, source, returnHandler);
        }

    }

    private class CatchReturnHandleInterceptor implements HandleInterceptor {

        @Override
        public void handle(ChannelInvocation<HandleInterceptor> invocation, Event event, BridgeConnector source) {

            if (event instanceof ReturnEventExtensionWrapper) {
                ReturnEventExtensionWrapper returnWrapper = (ReturnEventExtensionWrapper) event;

                if (!returnWrapper.isRequest()) {
                    handleReturnEvent(returnWrapper, source);
                    return;
                }
            }

            invocation.next().handle(invocation, event, source);
        }

        private void handleReturnEvent(ReturnEventExtensionWrapper returnEvent, BridgeConnector source) {

            long requestId = returnEvent.getRequestId();

            EventHandler<?> handler = returnHandlers.get(requestId);
            if (handler != null) {
                invokeReturnHandleChannel(returnEvent.getEvent(), source, handler);
                returnHandlers.remove(requestId);
            }
        }

        private void invokeReturnHandleChannel(Event returnEvent, BridgeConnector source, EventHandler<?> returnHandler) {

            ChannelInvocation<ReturnHandleInterceptor> invocation = returnHandleChannel.invoke();
            invocation.next().handleReturn(invocation, returnEvent, source, returnHandler);
        }

    }

}
