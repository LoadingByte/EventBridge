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

package com.quartercode.eventbridge;

import com.quartercode.eventbridge.def.bridge.module.DefaultStandardHandlerModule;
import com.quartercode.eventbridge.factory.Factory;

/**
 * A {@link Factory} for the {@link DefaultStandardHandlerModule} object.
 */
public class DefaultStandardHandlerModuleFactory implements Factory {

    @Override
    public Object create() {

        return new DefaultStandardHandlerModule();
    }

}
