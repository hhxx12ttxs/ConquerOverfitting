/*
 * 
 *  Copyright ÂŠ 2010, 2011 Inadco, Inc. All rights reserved.
 *  
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *  
 *         http://www.apache.org/licenses/LICENSE-2.0
 *  
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *  
 *  
 */
package com.inadco.hbl.math.aggregators;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.commons.lang.Validate;
import org.apache.hadoop.io.Writable;

/**
 * exponentially weighted average summarizer with irregular sampling
 * <P>
 * 
 * Supporting serializing state into {@link Writable}, updating-in-the-past and
 * combining summarizers over disjoint set of observations.
 * 
 * @author dmitriy
 * 
 */
public class OnlineExpAvgSummarizer implements IrregularSamplingSummarizer {

    public static double DEFAULT_HISTORY_MARGIN = 0.01d;

    // state
    protected double     w, s, t;
    // params
    protected double     alpha;

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * default constructor -- to be used with {@link Writable}, really.
     */

    public OnlineExpAvgSummarizer() {
        super();
        reset();
    }

    public void reset() {
        w = s = t = 0;
    }

    public OnlineExpAvgSummarizer(OnlineExpAvgSummarizer other) {
        this();
        assign(other);
    }

    @Override
    public void assign(IrregularSamplingSummarizer other) {
        Validate.isTrue(other instanceof OnlineExpAvgSummarizer);
        OnlineExpAvgSummarizer o = (OnlineExpAvgSummarizer) other;
        w = o.w;
        s = o.s;
        t = o.t;
        alpha = o.alpha;
    }

    /**
     * reasonable default parameters for margin.
     * 
     * @param dt
     *            exponential phase out to margin of 1% span length.
     */

    public OnlineExpAvgSummarizer(final double dt) {
        this(dt, DEFAULT_HISTORY_MARGIN);
    }

    /**
     * 
     * @param m
     *            history margin, exponential scale (alpha=0.01 means dt
     *            corresponds to -ln(0.01)*alpha
     * @param dt
     *            exponential phase out to margin m span length
     */
    public OnlineExpAvgSummarizer(double dt, double m) {
        super();
        if (dt < 0)
            throw new IllegalArgumentException("dt cannot be negative");
        if (m < 0 || m > 1)
            throw new IllegalArgumentException("margin must be in [0,1]");
        alpha = -dt / Math.log(m);

    }

    /**
     * add new event. Events must be added in ascending order only.
     * 
     * @param t
     * @param x
     * @return summed probability for moment t
     */
    @Override
    public double update(double x, double t) {
        if (t < this.t)
            return updatePast(x, t);
        return addFuture(x, t, true);
    }

    @Override
    public void combine(IrregularSamplingSummarizer other) {
        Validate.isTrue(other instanceof OnlineExpAvgSummarizer);
        OnlineExpAvgSummarizer o = (OnlineExpAvgSummarizer) other;

        Validate.isTrue(o.alpha == alpha, "Unable to combine incompatible summarizers: different exponential decay.");

        if (o.t == 0) {
            // do nothing -- other summarizer was empty

        } else if (t == 0) { // we did not have observations
            w = o.w;
            s = o.s;
            t = o.t;
        } else if (t >= o.t) {
            double pi = Math.exp((o.t - t) / alpha);
            w += pi * o.w;
            s += pi * o.s;
        } else {
            double pi = Math.exp((t - o.t) / alpha);
            w = o.w + pi * w;
            s = o.s + pi * s;
            t = o.t;
        }
    }

    @Override
    public void complement(IrregularSamplingSummarizer other, boolean artificialStretch) {
        Validate.isTrue(other instanceof OnlineExpAvgSummarizer);
        OnlineExpAvgSummarizer o = (OnlineExpAvgSummarizer) other;

        Validate.isTrue(o.alpha == alpha, "Unable to combine incompatible summarizers: different exponential decay.");

        Validate.isTrue(t >= o.t || artificialStretch,
                        "we are supposed to be a superset (this.t >= other.t) doesn't hold.");

        double pi;
        if (t < o.t) {
            // so apparently 'other' are newer observations and
            // we are missing some of the new observations in the
            // superset. So what we can do in this case, we can "stretch"
            // ourselves without events. Obviously, this would mean
            // we did not see what the other party saw.
            pi = Math.exp((t - o.t) / alpha);
            t = o.t;
            w = pi * w;
            s = pi * s;
            pi = 1;
        } else
            pi = Math.exp((o.t - t) / alpha);

        // another problem is that we don't really know if other corresponds to
        // last events or less than last events, so we can't correct t exactly.
        // This will affect stuff like biased estimators, because from their
        // point of view, there just were no recent observations. So complements
        // are probably not for biased estimates so much.
        s -= pi * o.s;
        w -= pi * o.w;
    }

    @Override
    public void combineBinomialOnes(IrregularSamplingSummarizer other) {
        Validate.isTrue(other instanceof OnlineExpAvgSummarizer);
        OnlineExpAvgSummarizer o = (OnlineExpAvgSummarizer) other;

        if (o.alpha != alpha)
            throw new IllegalArgumentException(
                "Unable to combine incompatible summarizers: different exponential decay.");

        if (o.t == 0) {
            // do nothing -- other summarizer was empty

        } else if (t == 0) { // we did not yet have observations
            // as a result, there's no weights in the combined history.
            // this actually may occur if RT jobs updated postive events but
            // haven't updated
            // negative events yet. So we don't update the state, assuming there
            // are no further summations in the pipeline

        } else if (t >= o.t) {
            double pi = Math.exp((o.t - t) / alpha);
            s += pi * o.s;
        } else {
            double pi = Math.exp((t - o.t) / alpha);
            s = o.s + pi * s;
            t = o.t;
        }
    }

    @Override
    public double getValue() {
        return getAvg();
    }

    @Override
    public double getValueNow(double tNow) {
        // it doesn't matter how much time has passed,
        // it won't change the value of the average.
        return getValue();
    }

    /**
     * Avg
     * 
     * @param t
     *            now, must be >= t<sub>n</sub>
     * @return the probability now
     */
    public double getAvg() {
        return w == 0 ? 0 : s / w;
    }

    protected double addFuture(double x, double t, boolean doUpdate) {
        double pi = this.t == 0 ? 0 : Math.exp((this.t - t) / alpha);
        double w = pi * this.w;
        double s = x + pi * this.s;
        if (doUpdate) {
            this.w = ++w;
            this.s = s;
            this.t = t;
            return this.s / this.w;
        }
        return s / w;
    }

    protected double updatePast(double x, double t) {
        double pi = Math.exp((t - this.t) / alpha);
        w += pi;
        s += pi * x;
        return s / w;
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        w = in.readDouble();
        s = in.readDouble();
        t = in.readDouble();
        alpha = in.readDouble();
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeDouble(w);
        out.writeDouble(s);
        out.writeDouble(t);
        out.writeDouble(alpha);
    }

    @Override
    public String toString() {
        return "OnlineExpAvgSummarizer [w=" + w + ", s=" + s + ", t=" + t + ", alpha=" + alpha + "]";
    }

}

