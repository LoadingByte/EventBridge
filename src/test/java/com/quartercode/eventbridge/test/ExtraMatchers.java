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

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import com.quartercode.eventbridge.bridge.EventPredicate;
import com.quartercode.eventbridge.bridge.module.LowLevelHandler;

public class ExtraMatchers {

    @Factory
    public static Matcher<LowLevelHandler> aLowLevelHandlerWithThePredicate(EventPredicate<?> predicate) {

        return new LowLevelHandlerPredicateMatcher(predicate);
    }

    private ExtraMatchers() {

    }

    private static class LowLevelHandlerPredicateMatcher extends TypeSafeMatcher<LowLevelHandler> {

        private final EventPredicate<?> predicate;

        private LowLevelHandlerPredicateMatcher(EventPredicate<?> predicate) {

            this.predicate = predicate;
        }

        @Override
        protected boolean matchesSafely(LowLevelHandler item) {

            return item.getPredicate().equals(predicate);
        }

        @Override
        public void describeTo(Description description) {

            description.appendText("a low-level handler with the predicate ").appendValue(predicate);
        }

    }

}
