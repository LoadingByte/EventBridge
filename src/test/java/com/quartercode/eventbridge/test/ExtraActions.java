
package com.quartercode.eventbridge.test;

import java.util.concurrent.atomic.AtomicReference;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;

public class ExtraActions {

    public static RecordValueActionBuilder recordArgument(int parameter) {

        return new RecordValueActionBuilder(parameter);
    }

    public ExtraActions() {

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
