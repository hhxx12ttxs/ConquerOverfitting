/*
 * Copyright 2006-2007 Tom Adams
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.googlecode.instinct.internal.expect.state.matcher;

import static com.googlecode.instinct.expect.state.matcher.EqualityMatcher.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;

public final class AssertExceptionImpl implements AssertException {
    public <T> Throwable assertWraps(final Class<T> expectedException, final Throwable wrapperException) {
        return assertWraps(expectedException, wrapperException, 1);
    }

    public <T> Throwable assertWraps(final Class<T> expectedException, final String expectedMessage, final Throwable wrapperException) {
        return assertWraps(expectedException, expectedMessage, wrapperException, 1);
    }

    public <T> Throwable assertWraps(final Class<T> expectedExceptionClass, final Throwable wrapperException,
            final int depthExceptionShouldAppearAt) {
        final int realDepth = depthExceptionShouldAppearAt <= 0 ? Integer.MAX_VALUE : depthExceptionShouldAppearAt;
        return checkWraps(expectedExceptionClass, wrapperException, realDepth);
    }

    public <T> Throwable assertWraps(final Class<T> expectedExceptionClass, final String expectedMessage, final Throwable wrapperException,
            final int depthExceptionShouldAppearAt) {
        final Throwable cause = assertWraps(expectedExceptionClass, wrapperException, depthExceptionShouldAppearAt);
        checkExceptionMessage(expectedMessage, cause);
        return cause;
    }

    public <T> void checkExceptionClass(final Class<T> expectedExceptionClass, final Throwable actual) {
        assertThat(actual, instanceOf(expectedExceptionClass));
    }

    public void checkExceptionMessage(final String expectedMessage, final Throwable actual) {
        assertThat(actual.getMessage(), equalTo(expectedMessage));
    }

    public <T> Throwable assertThrows(final Class<T> expectedException, final String message, final Runnable block) {
        final Throwable result = assertThrows(expectedException, block);
        checkExceptionMessage(message, result);
        return result;
    }

    // SUPPRESS IllegalCatch {
    @SuppressWarnings({"OverlyBroadCatchBlock"})
    public <T> Throwable assertThrows(final Class<T> expectedException, final Runnable block) {
        try {
            block.run();
            throw new AssertionError("Failed to throw exception: " + expectedException);
        } catch (Throwable t) {
            checkExceptionClass(expectedException, t);
            return t;
        }
    }// } SUPPRESS IllegalCatch

    private <T> Throwable checkWraps(final Class<T> expectedExceptionClass, final Throwable wrapperException, final int depth) {
        final Throwable cause = getCauseAtDepth(wrapperException, depth);
        checkExceptionClass(expectedExceptionClass, cause);
        return cause;
    }

    public void assertMessageContains(final Throwable t, final String fragment) {
        assertThat(t.getMessage(), containsString(fragment));
    }

    // SUPPRESS JavaNCSS {
    private Throwable getCauseAtDepth(final Throwable wrapperException, final int depth) {
        Throwable cause = wrapperException.getCause();
        boolean maxedOut = cause == wrapperException;
        int currentDepth = 1;
        while (!maxedOut && currentDepth < depth) {
            final Throwable currentWrapper = cause;
            cause = cause.getCause();
            maxedOut = cause == currentWrapper;
            currentDepth++;
        }
        if (depth != Integer.MAX_VALUE) {
            if (currentDepth != depth) {
                throw new AssertionError("Wrapped exception not found at expected depth of " + depth);
            }
        }
        return cause;
    }// } SUPPRESS JavaNCSS
}

