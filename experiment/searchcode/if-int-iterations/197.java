/*
 *  AlgorithmsRunner- A fancy and parallelized way to do my algorithms homework
Copyright (C) 2012 Joshua M. Hertlein

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.jmhertlein.cs253.knapsack;

import java.util.Random;
import net.jmhertlein.cs253.RuntimeTabulator;

/**
 *
 * @author joshua
 */
public class KnapsackTask implements Runnable {
    private Random gen;
    private Thief t;
    private int iterations, initSize, termSize;
    private RuntimeTabulator record;
    
    public KnapsackTask(int initSize, int termSize, int iterations, RuntimeTabulator record) {
        t = new GreedyThief();
        this.initSize = initSize;
        this.termSize = termSize;
        this.iterations = iterations;
        gen = new Random();
        this.record = record;
    }
    
    
    @Override
    public void run() {
        for(int iter = 0; iter < iterations; iter++) {
            for(int n = initSize; n <= termSize; n++) {
                //generate fresh data
                int[] w = new int[n], v = new int[n];
                for(int i = 0; i < n; i++) {
                    w[i] = gen.nextInt(100)+1;
                    v[i] = gen.nextInt(100)+1;
                }
                
                t.setSackCapacity(1000);
                
                //t.setSackCapacity((int) Math.pow(2, n));
                
                //int tentativeCapacity = (int) Math.pow(2, n);
                //t.setSackCapacity(tentativeCapacity > 100 ? tentativeCapacity : 101);
                
                //start timer
                long start = System.nanoTime();
                //fill sack
                t.fillKnapsack(v, w);
                //stop timer
                long stop = System.nanoTime();
                //log time
                record.logTime(n, stop-start);
            }
        }
    }
}

