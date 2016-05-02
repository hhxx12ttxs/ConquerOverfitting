/*
 * Copyright (c) 2008-2010, JFXtras Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of JFXtras nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.jfxtras.test.junit;

import org.jfxtras.test.*;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

/**
 * Callback helper for the JFXtrasRunner.
 * 
 * @author Stephen Chin
 */
public class JUnitTestCallback implements TestCallback {
    private Class clazz;
    private RunNotifier notifier;
    private Description lastDescription;

    public JUnitTestCallback(Class clazz, RunNotifier notifier) {
        this.clazz = clazz;
        this.notifier = notifier;
    }

    public void testSkipped(TestBase test, String description) {
        lastDescription = Description.createTestDescription(clazz, description);
        notifier.fireTestIgnored(lastDescription);
    }

    public void testStarted(TestBase test, String description) {
        lastDescription = Description.createTestDescription(clazz, description);
        notifier.fireTestStarted(lastDescription);
    }

    public void testSuccess(TestBase test) {
        notifier.fireTestFinished(lastDescription);
    }

    public void testFailure(TestBase test, Throwable throwable) {
        try {
            notifier.fireTestFailure(new Failure(lastDescription, throwable));
        } finally {
            notifier.fireTestFinished(lastDescription);
        }
    }

}

