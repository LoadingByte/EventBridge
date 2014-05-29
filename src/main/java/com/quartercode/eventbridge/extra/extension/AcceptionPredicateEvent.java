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

package com.quartercode.eventbridge.extra.extension;

import com.quartercode.eventbridge.basic.EventBase;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.EventPredicate;

/**
 * The add/remove predicate event is internally sent between two {@link Bridge}s for limiting the events that are sent
 * to the ones the other side is really interested in.
 * See the bridge javadoc for more details on the event limit system.<br>
 * <br>
 * <i>This class is internal and not intended to be used outside the bridge package.</i>
 * 
 * @see Bridge
 * @see EventPredicate
 */
public class AcceptionPredicateEvent extends EventBase {

    private static final long         serialVersionUID = 1615234521091371065L;

    private final EventPredicate<?>[] predicates;
    private final boolean             add;

    public AcceptionPredicateEvent(EventPredicate<?>[] predicates, boolean add) {

        this.predicates = predicates;
        this.add = add;
    }

    public EventPredicate<?>[] getPredicates() {

        return predicates;
    }

    public boolean isAdd() {

        return add;
    }

}
