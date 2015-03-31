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

package com.quartercode.eventbridge.basic;

import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.EventPredicate;

/**
 * An abstract {@link EventPredicate} class which already implements the {@code hashCode()} etc. methods using reflection builders.
 * It may be used as superclass for all event predicates.
 * 
 * @param <T> The type of event that can be tested by the predicate.
 */
public abstract class EventPredicateBase<T extends Event> extends DataObjectBase implements EventPredicate<T> {

    private static final long serialVersionUID = -6826299068985666013L;

}
