/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mobicents.media.server.scheduler;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author kulikov
 */
public class BasicPeriodicTask extends Task {

    private long priority;
    private long period;

    private long deviation;
    private volatile int count;

    private boolean first = true;
    private long lastTime;

    private volatile boolean isActive = true;
    private int missrate;
    private int missrate1;
    private long tolerance;

    private double[] drift = new double[1000];
    private long [] timeline = new long[1000];

    public BasicPeriodicTask(Scheduler scheduler, long priority, long period) {
        super(scheduler);
        this.priority = priority;
        this.period = period;
    }

    public double[] getDrift() {
        return drift;
    }

    public long[] getTimeline() {
        return timeline;
    }

    public long getPriority() {
        return this.priority;
    }

    public void setPriority(long priority) {
        this.priority = priority;
    }

    public boolean isActive() {
        return this.isActive;
    }

    public long getDuration() {
        return 0;
    }
    long lastTick = 0;
    long num = 0;
    public synchronized long perform() {
        count++;
        num++;
        if(count%10==0) {
        	System.out.println(System.currentTimeMillis() + " tick " + (System.currentTimeMillis() -lastTick) + " num="+num);
        }
        lastTick = System.currentTimeMillis();

        if (scheduler.getClock().getTime() - this.getDeadLine() > tolerance) {
            missrate1++;
        }
        
        if (first) {
            first = false;
            lastTime = scheduler.getClock().getTime();
        } else {
            long period_ = scheduler.getClock().getTime() - lastTime;
            long dev = Math.abs(period - period_);

            timeline[count] = scheduler.getClock().getTime();
            drift[count] = period - period_;
            
            if (dev > deviation) {
                this.deviation = dev;
            }

            //increment missrate countor if deviation exeedes limit
            if (dev > tolerance) {
                missrate++;
            }
            
            lastTime = scheduler.getClock().getTime();
        }

        //some work here
        long end = scheduler.getClock().getTime() + 1000000L;
        int i = 0;
        while (scheduler.getClock().getTime() < end) {
            i++;
        }

        //reschedule the task after period nanoseconds
        setDeadLine(getDeadLine() + period);
        final BasicPeriodicTask t = this;
        scheduler.submit(t);
        return 0;
    }

    public int getCount() {
        return count;
    }

    public long maxDeviation() {
        return this.deviation;
    }

    public void setTolerance(long tolerance) {
        this.tolerance = tolerance;
    }

    public int getMissRate() {
        return missrate;
    }

    public double missrate1() {
        return ((double)missrate1)/count;
    }
    public double missrate() {
        return ((double)(missrate)) / count;
    }

    public void report(String fileName) throws FileNotFoundException, IOException {
        FileOutputStream fout = new FileOutputStream(fileName);
        for (int i = 0; i < count; i++) {
            fout.write(String.format("%d %f\n", timeline[i], drift[i]).getBytes());
        }
        fout.flush();
        fout.close();
    }
}

