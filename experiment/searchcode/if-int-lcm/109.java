/* Copyright 2009 - 2010 The Stajistics Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.stajistics.aop;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

import org.stajistics.Stats;
import org.stajistics.StatsConstants;
import org.stajistics.StatsFactory;
import org.stajistics.StatsKey;
import org.stajistics.tracker.span.SpanTracker;

/**
 *
 *
 *
 * @author The Stajistics Project
 */
public class LifeCycleMonitor<T> {

    private final ReferenceQueue<T> refQueue = new ReferenceQueue<T>();

    private final Map<Reference<T>,SpanTracker> trackerMap =
        Collections.synchronizedMap(new IdentityHashMap<Reference<T>,SpanTracker>(512));

    private final LifeCyclePoller lifeCyclePoller = new LifeCyclePoller();

    private final StatsFactory factory;

    public LifeCycleMonitor() {
        this(null);
    }

    public LifeCycleMonitor(final StatsFactory factory) {
        if (factory == null) {
            this.factory = Stats.getFactory(StatsConstants.DEFAULT_NAMESPACE);
        } else {
            this.factory = factory;
        }
    }


    public void monitor(final T object,
                        final StatsKey key) {

        SpanTracker tracker = factory.track(key);

        Reference<T> ref = new PhantomReference<T>(object, refQueue);
        trackerMap.put(ref, tracker);
    }

    public boolean isRunning() {
        return lifeCyclePoller.isRunning();
    }

    public void shutdown() {
        lifeCyclePoller.shutdown();
    }

    public void reset() {
        trackerMap.clear();
    }

    protected void remove(final Reference<? extends T> ref) {
        SpanTracker tracker = trackerMap.remove(ref);
        if (tracker != null) {
            tracker.commit();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        shutdown();
        super.finalize();
    }

    /* NESTED CLASSES */

    private final class LifeCyclePoller implements Runnable {

        private volatile Thread thread;

        public LifeCyclePoller() {
            thread = new Thread(this, getThreadName());
            thread.start();
        }

        private String getThreadName() {
            return getClass().getSimpleName() + "@" + System.identityHashCode(this);
        }

        synchronized boolean isRunning() {
            return thread.isAlive();
        }

        synchronized void shutdown() {
            if (thread != null) {
                thread.interrupt();
                thread = null;
            }
        }

        @Override
        public void run() {
            try {
                final Thread currentThread = Thread.currentThread();

                Reference<? extends T> ref;

                while (thread == currentThread) {
                    ref = refQueue.remove();
                    LifeCycleMonitor.this.remove(ref);
                }

            } catch (InterruptedException e) {
                // Ignore

            } finally {
                thread = null;
            }
        }
    }

    public static void main(String[] args) throws Exception {

        StatsFactory factory = Stats.getFactory(StatsConstants.DEFAULT_NAMESPACE);
        LifeCycleMonitor<Object> lcm = new LifeCycleMonitor<Object>(factory);

        StatsKey key = factory.newKey("test");

        while (true) {
            for (int i = 0; i < 10000; i++) {
                lcm.monitor(new Object(), key);
            }

            Thread.sleep(5000);
        }
    }
}

