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

import org.apache.commons.lang3.mutable.Mutable;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;

public class ExtraActions {

    public static StoreArgumentActionBuilder storeArgument(int parameter) {

        return new StoreArgumentActionBuilder(parameter);
    }

    private ExtraActions() {

    }

    public static class StoreArgumentActionBuilder {

        private final int parameter;

        private StoreArgumentActionBuilder(int parameter) {

            this.parameter = parameter;
        }

        public Action in(Mutable<?> storage) {

            return new StoreArgumentAction<>(parameter, storage);
        }

    }

    private static class StoreArgumentAction<T> extends CustomAction {

        private final int        parameter;
        private final Mutable<T> storage;

        private StoreArgumentAction(int parameter, Mutable<T> storage) {

            super("stores objects in mutable wrapper");

            this.parameter = parameter;
            this.storage = storage;
        }

        @SuppressWarnings ("unchecked")
        @Override
        public Object invoke(Invocation invocation) {

            Object argument = invocation.getParameter(parameter);
            try {
                storage.setValue((T) argument);
            } catch (ClassCastException e) {
                throw new RuntimeException("Method argument '" + argument + "' cannot be stored");
            }

            return null;
        }

    }

}
