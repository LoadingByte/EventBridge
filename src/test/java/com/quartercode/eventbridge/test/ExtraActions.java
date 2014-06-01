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

import java.util.concurrent.atomic.AtomicReference;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;

public class ExtraActions {

    public static RecordValueActionBuilder recordArgument(int parameter) {

        return new RecordValueActionBuilder(parameter);
    }

    private ExtraActions() {

    }

    public static class RecordValueActionBuilder {

        private final int parameter;

        private RecordValueActionBuilder(int parameter) {

            this.parameter = parameter;
        }

        public Action to(AtomicReference<?> storage) {

            return new RecordValueAction<>(parameter, storage);
        }
    }

    private static class RecordValueAction<T> extends CustomAction {

        private final int                parameter;
        private final AtomicReference<T> storage;

        private RecordValueAction(int parameter, AtomicReference<T> storage) {

            super("stores LowLevelHandler in atomic reference");

            this.parameter = parameter;
            this.storage = storage;
        }

        @SuppressWarnings ("unchecked")
        @Override
        public Object invoke(Invocation invocation) {

            Object argument = invocation.getParameter(parameter);
            try {
                storage.set((T) argument);
            } catch (ClassCastException e) {
                throw new RuntimeException("Method argument '" + argument + "' cannot be recorded");
            }

            return null;
        }

    }

}
