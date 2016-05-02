/*
 * BEGIN_HEADER - DO NOT EDIT
 * 
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://open-jbi-components.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://open-jbi-components.dev.java.net/public/CDDLv1.0.html.
 * If applicable add the following below this CDDL HEADER,
 * with the fields enclosed by brackets "[]" replaced with
 * your own identifying information: Portions Copyright
 * [year] [name of copyright owner]
 */

/*
 * @(#)Main.java 
 *
 * Copyright 2004-2007 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * END_HEADER - DO NOT EDIT
 */

package corrSample;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    /**
     * @param args
     * @throws InterruptedException 
     */
    public static void main(String[] args) throws InterruptedException {
        // TODO Auto-generated method stub
        int iterations = Integer.parseInt(args[0]);
        int threadCount = Integer.parseInt(args[1]);
        WSInvoker invokerArray[] = new WSInvoker[threadCount];

        CorrelatedBpelClientService service = new CorrelatedBpelClientService();
        CorrelatedBpelClientPortType port = service.getCorrelatedBpelClientPort();
        for (int i = 0; i < threadCount; i++) {
            invokerArray[i] = new WSInvoker(port, i, iterations);
        }

        long start = System.currentTimeMillis();

        ExecutorService es = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            es.submit(invokerArray[i]);
        }
        long middle = System.currentTimeMillis();
        es.shutdown();
        es.awaitTermination(5760l, TimeUnit.SECONDS);

        long end = System.currentTimeMillis();

        System.out.println("W:" + (end - middle) + ",R:" + iterations / ((end - start) / 1000.0));

        long minLatency = 0, peekLatency = 0;
        double avgLatency = 0, totalLatency = 0;

        minLatency = invokerArray[0].getMinLatency();
        for (int i = 0; i < threadCount; i++) {
            totalLatency = totalLatency + invokerArray[i].getAvgLatency();
            if (invokerArray[i].getMinLatency() < minLatency) {
                minLatency = invokerArray[i].getMinLatency();
            }
            if (invokerArray[i].getPeekLatency() > peekLatency) {
                peekLatency = invokerArray[i].getPeekLatency();
            }
        }
        avgLatency = totalLatency / threadCount;

        System.out.println("Latency info: avg:" + avgLatency + ", min:" + minLatency + ", peek:" + peekLatency);
    }

}

