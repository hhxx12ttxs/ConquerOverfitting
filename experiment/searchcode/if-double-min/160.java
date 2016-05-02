<<<<<<< HEAD
package uk.ac.rhul.cs.dice.golem.conbine.agent.williams.utils;

import java.util.Random;

public class RandomBidCreator implements BidCreator {

    protected final Random random;
    private final double initialPrice;
    private final double reservationPrice;

    public RandomBidCreator(double initialPrice, double reservationPrice) {
        random = new Random();
        this.initialPrice = initialPrice;
        this.reservationPrice = reservationPrice;
    }

    public double getIP() {
        return initialPrice;
    }

    public double getRP() {
	    return reservationPrice;
	}

	public double getUtility(double offer) {
	    return (getRP() - offer) / (getRP() - getIP());
	}

	@Override
	public double getBid(double utilitySpace, double min, double max) {
	    return getRandomBid(utilitySpace, min, max);
	}

	/**
     * Get a random bid.
     * 
     * @param utilitySpace
     *            The utility space to generate the random bid from.
     * @return a random bid.
     */
    private double getRandomBid(double utilitySpace) {
        return getIP() + random.nextInt((int) (getRP() - getIP()) + 1);
    }
    
    /**
     * Get a random bid (above a minimum utility value if possible).
     * 
     * @param utilitySpace
     *            The utility space to generate the random bid from.
     * @param min
     *            The minimum utility value.
     * @return a random bid (above a minimum utility value if possible).
     */
    private double getRandomBid(double utilitySpace, double min) {
        int i = 0;
        while (true) {
            double b = getRandomBid(utilitySpace);
            double util = getUtility(b);
//            Logger.i(this, "b:" + b + "util: " + util);
            
            if (util >= min) {
//                Logger.i(this, "util >= min " + "util:" + util + " min: " +  min);
                //printVal(util);
                return b;
            }
            
            i++;
            
            if (i == 500) {
//            	Logger.i(this, "i == 500");
                min -= 0.01;
//                Logger.i(this, "min: " + min);
                i = 0;
            }
        }
    }

    /**
     * Get a random bid (within a utility range if possible).
     * 
     * @param utilitySpace
     *            The utility space to generate the random bid from.
     * @param min
     *            The minimum utility value.
     * @param max
     *            The maximum utility value.
     * @return a random bid (within a utility range if possible).
     */
    public double getRandomBid(double utilitySpace, double min, double max) {
       // printRange(min, max);
        //System.out.println("Get bid in range ["+min+", "+max+"]");
        int i = 0;
        while (true) {
            if (max >= 1) {
                return getRandomBid(utilitySpace, min);
            }
            
            double b = getRandomBid(utilitySpace);
            double util = getUtility(b);
            
            if (util >= min && util <= max) {
               // printVal(util);
                return b;
            }
            
            i++;
            
            if (i == 500) {
                max += 0.01;
                i = 0;
            }
        }
    }

    private void printRange(double min, double max) {
        min = Math.max(min, 0);
        max = Math.min(max, 1);
        int i = 0;
        for (; i < min * 100; i++) {
            System.out.print(" ");
        }
        for (; i < max * 100; i++) {
            System.out.print("-");
        }
        System.out.println();
    }

    private void printVal(double util) {
        for (int i = 0; i < util * 100; i++) {
            System.out.print(" ");
        }
        System.out.println("^");
=======
/*
 * Licensed to Elastic Search and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Elastic Search licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.search.facet.statistical;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentBuilderString;
import org.elasticsearch.search.facet.Facet;
import org.elasticsearch.search.facet.InternalFacet;

import java.io.IOException;

/**
 * @author kimchy (shay.banon)
 */
public class InternalStatisticalFacet implements StatisticalFacet, InternalFacet {

    private static final String STREAM_TYPE = "statistical";

    public static void registerStreams() {
        Streams.registerStream(STREAM, STREAM_TYPE);
    }

    static Stream STREAM = new Stream() {
        @Override public Facet readFacet(String type, StreamInput in) throws IOException {
            return readStatisticalFacet(in);
        }
    };

    @Override public String streamType() {
        return STREAM_TYPE;
    }

    private String name;

    private double min;

    private double max;

    private double total;

    private double sumOfSquares;

    private long count;

    private InternalStatisticalFacet() {
    }

    public InternalStatisticalFacet(String name, double min, double max, double total, double sumOfSquares, long count) {
        this.name = name;
        this.min = min;
        this.max = max;
        this.total = total;
        this.sumOfSquares = sumOfSquares;
        this.count = count;
    }

    @Override public String name() {
        return this.name;
    }

    @Override public String getName() {
        return name();
    }

    @Override public String type() {
        return TYPE;
    }

    @Override public String getType() {
        return TYPE;
    }

    @Override public long count() {
        return this.count;
    }

    @Override public long getCount() {
        return count();
    }

    @Override public double total() {
        return this.total;
    }

    @Override public double getTotal() {
        return total();
    }

    @Override public double sumOfSquares() {
        return this.sumOfSquares;
    }

    @Override public double getSumOfSquares() {
        return sumOfSquares();
    }

    @Override public double mean() {
        if (count == 0) {
            return 0;
        }
        return total / count;
    }

    @Override public double getMean() {
        return mean();
    }

    @Override public double min() {
        return this.min;
    }

    @Override public double getMin() {
        return min();
    }

    @Override public double max() {
        return this.max;
    }

    @Override public double getMax() {
        return max();
    }

    public double variance() {
        return (sumOfSquares - ((total * total) / count)) / count;
    }

    public double getVariance() {
        return variance();
    }

    public double stdDeviation() {
        return Math.sqrt(variance());
    }

    public double getStdDeviation() {
        return stdDeviation();
    }

    static final class Fields {
        static final XContentBuilderString _TYPE = new XContentBuilderString("_type");
        static final XContentBuilderString COUNT = new XContentBuilderString("count");
        static final XContentBuilderString TOTAL = new XContentBuilderString("total");
        static final XContentBuilderString MIN = new XContentBuilderString("min");
        static final XContentBuilderString MAX = new XContentBuilderString("max");
        static final XContentBuilderString MEAN = new XContentBuilderString("mean");
        static final XContentBuilderString SUM_OF_SQUARES = new XContentBuilderString("sum_of_squares");
        static final XContentBuilderString VARIANCE = new XContentBuilderString("variance");
        static final XContentBuilderString STD_DEVIATION = new XContentBuilderString("std_deviation");
    }

    @Override public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(name);
        builder.field(Fields._TYPE, StatisticalFacet.TYPE);
        builder.field(Fields.COUNT, count());
        builder.field(Fields.TOTAL, total());
        builder.field(Fields.MIN, min());
        builder.field(Fields.MAX, max());
        builder.field(Fields.MEAN, mean());
        builder.field(Fields.SUM_OF_SQUARES, sumOfSquares());
        builder.field(Fields.VARIANCE, variance());
        builder.field(Fields.STD_DEVIATION, stdDeviation());
        builder.endObject();
        return builder;
    }

    public static StatisticalFacet readStatisticalFacet(StreamInput in) throws IOException {
        InternalStatisticalFacet facet = new InternalStatisticalFacet();
        facet.readFrom(in);
        return facet;
    }

    @Override public void readFrom(StreamInput in) throws IOException {
        name = in.readUTF();
        count = in.readVLong();
        total = in.readDouble();
        min = in.readDouble();
        max = in.readDouble();
        sumOfSquares = in.readDouble();
    }

    @Override public void writeTo(StreamOutput out) throws IOException {
        out.writeUTF(name);
        out.writeVLong(count);
        out.writeDouble(total);
        out.writeDouble(min);
        out.writeDouble(max);
        out.writeDouble(sumOfSquares);
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }
}

