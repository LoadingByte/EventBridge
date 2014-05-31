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
import org.apache.commons.lang3.Validate;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.BridgeConnectorException;
import com.quartercode.eventbridge.bridge.BridgeModule;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.module.HandlerModule;
import com.quartercode.eventbridge.bridge.module.SenderModule;
import com.quartercode.eventbridge.def.bridge.module.DefaultConnectorSenderModule;
import com.quartercode.eventbridge.def.bridge.module.DefaultHandlerModule;
import com.quartercode.eventbridge.def.bridge.module.DefaultLocalHandlerSenderModule;
import com.quartercode.eventbridge.def.bridge.module.DefaultSenderModule;

/**
 * The default implementation of the {@link Bridge} interface.
 * 
 * @see Bridge
 */
public class DefaultBridge implements Bridge {

    private final List<BridgeModule>                modules                      = new ArrayList<>();

    private final List<BridgeConnector>             connectors                   = new ArrayList<>();
    private final List<ModifyConnectorListListener> modifyConnectorListListeners = new ArrayList<>();
    private List<BridgeConnector>                   connectorsUnmodifiableCache;

    /**
     * Creates a new default bridge.
     */
    public DefaultBridge() {

        addModule(new DefaultSenderModule());
        addModule(new DefaultConnectorSenderModule());
        addModule(new DefaultLocalHandlerSenderModule());

        addModule(new DefaultHandlerModule());
    }

    // ----- Modules -----

    @Override
    public <T extends BridgeModule> T getModule(Class<T> type) {

        Validate.notNull(type, "Module type for module retrieval cannot be null");

        for (BridgeModule module : modules) {
            if (type.isInstance(module)) {
                return type.cast(module);
            }
        }

        return null;
    }

    @Override
    public void addModule(BridgeModule module) {

        Validate.notNull(module, "The module to add to a bridge cannot be null");
        Validate.isTrue(getModule(module.getClass()) == null, "Module of type '%s' is already added to the bridge", module.getClass().getName());

        modules.add(module);
        module.add(this);
    }

    @Override
    public void removeModule(BridgeModule module) {

        Validate.notNull(module, "The module to remove from a bridge cannot be null");

        if (modules.contains(module)) {
            module.remove();
            modules.remove(module);
        }
    }

    // ----- Shortcuts -----

    @Override
    public void send(Event event) {

        getModule(SenderModule.class).send(event);
    }

    @Override
    public void handle(BridgeConnector source, Event event) {

        getModule(HandlerModule.class).handle(source, event);
    }

    // ----- Connectors -----

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

        try {
            connector.start(this);
        } catch (BridgeConnectorException e) {
            connectors.remove(connector);
            connectorsUnmodifiableCache = null;
            throw e;
        }

        for (ModifyConnectorListListener listener : modifyConnectorListListeners) {
            listener.onAddConnector(connector, this);
        }
    }

    @Override
    public void removeConnector(BridgeConnector connector) throws BridgeConnectorException {

        if (connectors.contains(connector)) {
            for (ModifyConnectorListListener listener : modifyConnectorListListeners) {
                listener.onRemoveConnector(connector, this);
            }

            try {
                connector.stop();
            } finally {
                connectors.remove(connector);
                connectorsUnmodifiableCache = null;
            }
        }
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
