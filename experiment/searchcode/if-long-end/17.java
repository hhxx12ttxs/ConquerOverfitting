/**
 * Copyright (C) 2009 Mathieu Carbou <mathieu.carbou@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mycila.event.spi;

import com.mycila.event.api.Dispatcher;
import com.mycila.event.api.Event;
import com.mycila.event.api.Subscriber;
import com.mycila.event.api.Topics;
import org.junit.Ignore;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
@Ignore
final class PerfTest {
    public static void main(String... args) throws InterruptedException {
        Map<String, Dispatcher> dispatchers = new LinkedHashMap<String, Dispatcher>(6) {
            {
                put("SynchronousSafe", Dispatchers.synchronousSafe(ErrorHandlers.rethrowErrorsImmediately()));
                put("SynchronousUnsafe", Dispatchers.synchronousUnsafe(ErrorHandlers.rethrowErrorsImmediately()));
                put("AsynchronousSafe", Dispatchers.asynchronousSafe(ErrorHandlers.rethrowErrorsImmediately()));
                put("AsynchronousUnsafe", Dispatchers.asynchronousUnsafe(ErrorHandlers.rethrowErrorsImmediately()));
                put("BroadcastOrdered", Dispatchers.broadcastOrdered(ErrorHandlers.rethrowErrorsImmediately()));
                put("BroadcastUnordered", Dispatchers.broadcastUnordered(ErrorHandlers.rethrowErrorsImmediately()));
            }
        };
        for (Map.Entry<String, Dispatcher> entry : dispatchers.entrySet()) {
            System.out.println("===== " + entry.getKey() + " Statistics =====");
            entry.getValue().subscribe(Topics.only("stats"), StatEvent.class, new Subscriber<StatEvent>() {
                @Override
                public void onEvent(Event<StatEvent> statEvent) throws Exception {
                    statEvent.getSource().received();
                }
            });
            test(entry.getValue(), 1000, 500);
            test(entry.getValue(), 1000, 1000);
        }
        System.out.println("\nFinished!");
    }

    static void test(final Dispatcher dispatcher, int nPublishers, int nEvents) throws InterruptedException {
        System.out.println("Ramp up...");
        for (int i = 0; i < 50; i++)
            publish(dispatcher, 1000, 100);
        System.out.println("Testing " + nPublishers + " consumers sending " + nEvents + " events each");
        long start = System.nanoTime();
        List<StatEvent> events = publish(dispatcher, nPublishers, nEvents);
        long end = System.nanoTime();
        long latency = 0;
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        for (StatEvent event : events) {
            latency += event.latency();
            if (event.latency() > max)
                max = event.latency();
            else if (event.latency() < min)
                min = event.latency();
        }
        latency /= events.size();
        System.out.println("- exec. time: " + (end - start) + "ns = " + TimeUnit.MILLISECONDS.convert(end - start, TimeUnit.NANOSECONDS) + "ms");
        System.out.println("- avg. latency: " + latency + "ns = " + TimeUnit.MILLISECONDS.convert(latency, TimeUnit.NANOSECONDS) + "ms");
        System.out.println("- min. latency: " + min + "ns = " + TimeUnit.MILLISECONDS.convert(min, TimeUnit.NANOSECONDS) + "ms");
        System.out.println("- max. latency: " + max + "ns = " + TimeUnit.MILLISECONDS.convert(max, TimeUnit.NANOSECONDS) + "ms");
        System.out.println("Cleaning up thread pool...");
        Thread.sleep(15000);
    }

    static List<StatEvent> publish(final Dispatcher dispatcher, int nPublishers, int nEvents) {
        final ConcurrentLinkedQueue<StatEvent> list = new ConcurrentLinkedQueue<StatEvent>();
        final CountDownLatch go = new CountDownLatch(1);
        final CountDownLatch finished = new CountDownLatch(nPublishers);
        for (int i = 0; i < nPublishers; i++) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        go.await();
                        dispatcher.publish(Topics.topic("stats"), new StatEvent(list));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        finished.countDown();
                    }
                }
            }.start();
        }

        go.countDown();
        try {
            finished.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return new LinkedList<StatEvent>(list);
    }

    static final class StatEvent {
        final Queue<StatEvent> queue;
        final long start;
        long end;

        StatEvent(Queue<StatEvent> queue) {
            this.start = System.nanoTime();
            this.queue = queue;
        }

        void received() {
            end = System.nanoTime();
            queue.add(this);
        }

        long latency() {
            return end - start;
        }
    }
}

/*

On DELL laptopp inspiron 6400:

===== SynchronousSafe Statistics =====
Testing 1000 consumers sending 500 events each
- exec. time: 166894057ns = 166ms
- avg. latency: 6427ns = 0ms
- min. latency: 3074ns = 0ms
- max. latency: 374637ns = 0ms
Testing 1000 consumers sending 1000 events each
- exec. time: 167090732ns = 167ms
- avg. latency: 475583ns = 0ms
- min. latency: 3073ns = 0ms
- max. latency: 4131488ns = 4ms
===== SynchronousUnsafe Statistics =====
Testing 1000 consumers sending 500 events each
- exec. time: 162852777ns = 162ms
- avg. latency: 3958ns = 0ms
- min. latency: 3003ns = 0ms
- max. latency: 144993ns = 0ms
Testing 1000 consumers sending 1000 events each
- exec. time: 308882709ns = 308ms
- avg. latency: 6039ns = 0ms
- min. latency: 4330ns = 0ms
- max. latency: 286775ns = 0ms
===== AsynchronousSafe Statistics =====
Testing 1000 consumers sending 500 events each
- exec. time: 154075405ns = 154ms
- avg. latency: 153753ns = 0ms
- min. latency: 4959ns = 0ms
- max. latency: 1908249ns = 1ms
Testing 1000 consumers sending 1000 events each
- exec. time: 163709151ns = 163ms
- avg. latency: 90922ns = 0ms
- min. latency: 5028ns = 0ms
- max. latency: 1452856ns = 1ms
===== AsynchronousUnsafe Statistics =====
Testing 1000 consumers sending 500 events each
- exec. time: 171095881ns = 171ms
- avg. latency: 51983ns = 0ms
- min. latency: 4261ns = 0ms
- max. latency: 2767426ns = 2ms
Testing 1000 consumers sending 1000 events each
- exec. time: 179646737ns = 179ms
- avg. latency: 108732ns = 0ms
- min. latency: 4191ns = 0ms
- max. latency: 50084653ns = 50ms
===== BroadcastOrdered Statistics =====
Testing 1000 consumers sending 500 events each
- exec. time: 184653243ns = 184ms
- avg. latency: 11617597ns = 11ms
- min. latency: 30102ns = 0ms
- max. latency: 25190883ns = 25ms
Testing 1000 consumers sending 1000 events each
- exec. time: 195372864ns = 195ms
- avg. latency: 5811118ns = 5ms
- min. latency: 18788ns = 0ms
- max. latency: 9696398ns = 9ms
===== BroadcastUnordered Statistics =====
Testing 1000 consumers sending 500 events each
- exec. time: 288626478ns = 288ms
- avg. latency: 309981ns = 0ms
- min. latency: 12711ns = 0ms
- max. latency: 6885810ns = 6ms
Testing 1000 consumers sending 1000 events each
- exec. time: 195777764ns = 195ms
- avg. latency: 1837885ns = 1ms
- min. latency: 34781ns = 0ms
- max. latency: 73457209ns = 73ms

*/
