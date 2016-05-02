/*
 * Copyright 1999-2005 The Apache Software Foundation.
 * Copyright 2006 Simon Pepping.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id: MinOptMax.java 33 2006-06-07 18:21:56Z simon $ */

package cc.creativecomputing.gui.text.linebreaking;

/**
 * This class was copied from Apache FOP, org.apache.fop.traits.MinOptMax.
 * It was slightly modified.
 * 
 * This class holds the resolved (as mpoints) form of a LengthRange or
 * Space type Property value.
 * MinOptMax values are used during layout calculations. The instance
 * variables are package visible.
 */
public class MinOptMax implements Cloneable {

    /** min(imum), opt(imum) and max(imum) values */
    private float min;
    private float opt;
    private float max;
    /**
     * The first-order infinite component, like TeX's fil.
     * It cannot be manipulated directly.
     * It is created by a constructor with max = Integer.MAX_VALUE.
     * It is modified by addition of another MinOptMax object,
     * or by addition of a triple (min, opt, max) with max = Integer.MAX_VALUE.
     */
    private int maxfil;

    /**
     * New min/opt/max with zero values.
     */
    public MinOptMax() {
        this(0);
    }

    /**
     * New min/opt/max with one fixed value.
     *
     * @param val the value for min, opt and max
     */
    public MinOptMax(float val) {
        this(val, val, val);
    }

    /**
     * New min/opt/max with the three values.
     *
     * @param min the minimum value
     * @param opt the optimum value
     * @param max the maximum value
     */
    public MinOptMax(float min, float opt, float max) {
        this.min = min;
        this.opt = opt;
        if (max == Integer.MAX_VALUE) {
            this.maxfil = 1;
            this.max = 0;
        } else {
            this.max = max;
            this.maxfil = 0;
        }
    }

    /**
     * Copy constructor.
     *
     * @param op the MinOptMax object to copy
     */
    public MinOptMax(MinOptMax op) {
        this.min = op.min;
        this.opt = op.opt;
        this.max = op.max;
        this.maxfil = op.maxfil;
    }

    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException ex) {
            // SHOULD NEVER OCCUR - all members are primitive types!
            return null;
        }
    }

    /**
     * Adds one MinOptMax instance to another returning a new one.
     * @param op1 first instance
     * @param op2 second instance
     * @return MinOptMax new instance
     */
    public static MinOptMax add(MinOptMax op1, MinOptMax op2) {
        MinOptMax mom = new MinOptMax(op1.min + op2.min, op1.opt + op2.opt, op1.max + op2.max);
        mom.maxfil = op1.maxfil + op2.maxfil;
        return mom;
    }

    /**
     * Decrements one MinOptMax instance with another returning a new one.
     * @param op1 first instance
     * @param op2 second instance
     * @return MinOptMax new instance
     */
    public static MinOptMax decr(MinOptMax op1, MinOptMax op2) {
        MinOptMax mom = new MinOptMax(op1.min - op2.min, op1.opt - op2.opt,
                                      op1.max - op2.max);
        mom.maxfil = op1.maxfil - op2.maxfil;
        return mom;
    }

    /**
     * Multiplies a MinOptMax instance with a factor returning a new instance.
     * @param op1 MinOptMax instance
     * @param mult multiplier
     * @return MinOptMax new instance
     */
    public static MinOptMax multiply(MinOptMax op1, double mult) {
        MinOptMax mom = new MinOptMax((int)(op1.min * mult),
                                      (int)(op1.opt * mult), (int)(op1.max * mult));
        // TODO: Should the infinite component also be multiplied?
        mom.maxfil = (int) (op1.maxfil * mult);
        return mom;
    }

    /**
     * Adds another MinOptMax instance to this one.
     * @param op the other instance
     */
    public void add(MinOptMax op) {
        min += op.min;
        opt += op.opt;
        max += op.max;
        maxfil += op.maxfil;
    }

    /**
     * Adds min, opt and max to their counterpart components.
     * @param min the value to add to the minimum value
     * @param opt the value to add to the optimum value
     * @param max the value to add to the maximum value
     */
    public void add(int min, int opt, int max) {
        this.min += min;
        this.opt += opt;
        if (max == Integer.MAX_VALUE) {
            this.maxfil += 1;
        } else {
            this.max += max;
        }
    }

    /**
     * Adds a length to all components.
     * @param len the length to add
     */
    public void add(int len) {
        this.min += len;
        this.opt += len;
        this.max += len;
    }

    /**
     * Decreases this MinOptMax instance with another one.
     * Used when backtracking over suppressed items.
     * @param op the other instance
     */
    public void decr(MinOptMax op) {
        min -= op.min;
        opt -= op.opt;
        max -= op.max;
        maxfil -= op.maxfil;
    }

    /** @return true if this instance represents a zero-width length (min=opt=max=0) */
    public boolean isNonZero() {
        return (min != 0 || max != 0 || maxfil != 0);
    }

    /** @return true if this instance allows for shrinking or stretching */
    public boolean isElastic() {
        return (min != opt || opt != max || maxfil != 0);
    }
    
    /** @see java.lang.Object#toString() */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("MinOptMax[min=");
        if (min != opt) {
            sb.append(min).append("; ");
        }
        sb.append("opt=");
        if (opt != max || maxfil != 0) {
            sb.append(opt).append("; ");
        }
        sb.append("max=").append(max);
        if (maxfil != 0) {
            sb.append("+" + maxfil + "fil");
        }
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * This method returns an integer, not the compound value max and maxfil!
     * @return Returns the max.
     */
    public float getMax() {
        if (maxfil != 0) {
            return Integer.MAX_VALUE;
        } else {
            return max;
        }
    }
    
    /**
     * @return Returns the min.
     */
    public float getMin() {
        return min;
    }
    
    /**
     * @return Returns the opt.
     */
    public float getOpt() {
        return opt;
    }

    public float getStretch() {
        if (maxfil != 0) {
            return Integer.MAX_VALUE;
        } else {
            return max - opt;
        }
    }
    
    public float getShrink() {
        return opt - min;
    }
    
    public boolean hasInfiniteStretch() {
        return maxfil != 0;
    }
    
}


