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

import static org.junit.Assert.assertTrue;
import java.util.List;
import java.util.Objects;

public class ExtraAssert {

    public static void assertListEquals(String message, List<?> list, Object... elements) {

        assertTrue(message, list.size() == elements.length);

        for (int index = 0; index < list.size(); index++) {
            assertTrue(message, Objects.equals(elements[index], list.get(index)));
        }
    }

    private ExtraAssert() {

    }

}
