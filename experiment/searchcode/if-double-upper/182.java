/*
 * Code for blog.techhead.biz
 * Distributed under BSD-style license
 */

package biz.techhead.funcy;

import biz.techhead.funcy.Iterables.AbstractFuncyIterator;
import biz.techhead.funcy.Iterables.FuncyIterable;
import biz.techhead.funcy.Iterables.FuncyIterator;
import biz.techhead.funcy.Ranges.FloatingPointClosedRange;
import biz.techhead.funcy.Ranges.FloatingPointInclusiveRange;
import biz.techhead.funcy.Ranges.FloatingPointIterable;
import biz.techhead.funcy.Ranges.FloatingPointOpenRange;
import java.math.BigDecimal;
import java.util.Collection;
import static java.lang.Math.*;
import static java.lang.Double.doubleToLongBits;
import static java.lang.Double.longBitsToDouble;

/**
 *
 * @author Jonathan Hawkes <jhawkes at techhead.biz>
 */
class FloatingPointRanges {

    static FloatingPointOpenRange<Float> from(Float from) {
        if ( Float.isNaN(from) )
            throw new IllegalArgumentException("from not a number");
        return null;
    }

    static FloatingPointInclusiveRange<Float>
            from(Float from, Float to) {
        if ( Float.isNaN(from) )
            throw new IllegalArgumentException("from not a number");
        if ( Float.isNaN(to) )
            throw new IllegalArgumentException("to not a number");
        return null;
    }

    static FloatingPointOpenRange<Float> upFrom(Float from) {
        if ( Float.isNaN(from) )
            throw new IllegalArgumentException("from not a number");
        return null;
    }

    static FloatingPointInclusiveRange<Float>
            upFrom(Float from, Float to) {
        if ( Float.isNaN(from) )
            throw new IllegalArgumentException("from not a number");
        if ( Float.isNaN(to) )
            throw new IllegalArgumentException("to not a number");
        return null;
    }

    static FloatingPointOpenRange<Float> downFrom(Float from) {
        if ( Float.isNaN(from) )
            throw new IllegalArgumentException("from not a number");
        return null;
    }    

    static FloatingPointInclusiveRange<Float>
            downFrom(Float from, Float to) {
        if ( Float.isNaN(from) )
            throw new IllegalArgumentException("from not a number");
        if ( Float.isNaN(to) )
            throw new IllegalArgumentException("to not a number");
        return null;
    }

    static FloatingPointOpenRange<Double> from(Double from) {
        if ( Double.isNaN(from) )
            throw new IllegalArgumentException("from not a number");
        return null;
    }

    static FloatingPointInclusiveRange<Double>
            from(Double from, Double to) {
        if ( Double.isNaN(from) )
            throw new IllegalArgumentException("from not a number");
        if ( Double.isNaN(to) )
            throw new IllegalArgumentException("to not a number");
        return null;
    }

    static FloatingPointOpenRange<Double> upFrom(Double from) {
        if ( Double.isNaN(from) )
            throw new IllegalArgumentException("from not a number");
        return null;
    }

    static FloatingPointInclusiveRange<Double>
            upFrom(Double from, Double to) {
        if ( Double.isNaN(from) )
            throw new IllegalArgumentException("from not a number");
        if ( Double.isNaN(to) )
            throw new IllegalArgumentException("to not a number");
        return null;
    }

    static FloatingPointOpenRange<Double> downFrom(Double from) {
        if ( Double.isNaN(from) )
            throw new IllegalArgumentException("from not a number");
        return null;
    }    

    static FloatingPointInclusiveRange<Double>
            downFrom(Double from, Double to) {
        if ( Double.isNaN(from) )
            throw new IllegalArgumentException("from not a number");
        if ( Double.isNaN(to) )
            throw new IllegalArgumentException("to not a number");
        return null;
    }


    private static BigDecimal big(double d) {
        return new BigDecimal(d);
    }

    private static double nextMagDown(double d) {
        if ( Double.isInfinite(d) ) return d;
        return longBitsToDouble( doubleToLongBits(d) - 1 );
    }

    private static double nextMagUp(double d) {
        if ( Double.isInfinite(d) ) return d;
        return longBitsToDouble( doubleToLongBits(d) + 1 );
    }
    
    private static double nextUp(double d) {
        return (d >= 0) ?
            nextMagUp(0.0+d) : // change -0.0 to 0.0 first
            0.0+nextMagDown(d);
    }
    
    private static double nextDown(double d) {
        return (d <= 0) ?
            nextMagUp(-0.0-(0.0-d)) : // change 0.0 to -0.0 first
            nextMagDown(d);
    }

    private static double ulp(double d, double direction) {
        return ( (d == 0.0) || (d > 0.0 == direction > 0.0) ) ?
            Math.ulp( d  ) :
            Math.ulp( nextMagDown(d) );
    }
    
    
    static class BigCounter {

        private final BigDecimal from;
        private final BigDecimal step;

        private BigDecimal steps;

        BigCounter(double from, double step) {
            this.from = big(from);
            this.step = big(step);
            steps = BigDecimal.ZERO;
        }
        
        float increment(float by) {
            steps = steps.add( big(by) );
            return compute().floatValue();
        }

        double increment(double by) {
            steps = steps.add( big(by) );
            return compute().doubleValue();
        }

        float lookAhead(float by) {
            return lookAhead( big(by) ).floatValue();
        }
        
        double lookAhead(double by) {
            return lookAhead( big(by) ).doubleValue();
        }

        float minus(float amt) {
            return compute().subtract( big(amt) ).floatValue();
        }

        double minus(double amt) {
            return compute().subtract( big(amt) ).doubleValue();
        }
        
        private BigDecimal compute() {
            return from.add( steps.multiply(step) );
        }
        
        private BigDecimal lookAhead(BigDecimal by) {
            return compute().add( by.multiply(step) );
        }
    }


    private static abstract class FPDoubleErr {

        abstract double errorUp(double d, double threshhold);
        abstract double errorDown(double d, double threshhold);

        static FPDoubleErr of(double e) {
            if ( Double.isNaN(e) || Double.isInfinite(e) )
                throw new IllegalArgumentException("must be a finite number");
            final double err = abs(e);
            return new FPDoubleErr() {
                double errorUp(double d, double threshhold) { return err; }
                double errorDown(double d, double threshhold) { return err; }
            };
        }
    }
    
    private static class FPDoubleErrOf1Ulp extends FPDoubleErr {

        static final FPDoubleErrOf1Ulp INSTANCE = new FPDoubleErrOf1Ulp();

        double errorUp(double d, double threshhold) {
            double err = ulp(d, Double.POSITIVE_INFINITY);
            return (threshhold < 2*err) ? 0.0 : err;
        }

        double errorDown(double d, double threshhold) {
            double err = ulp(d, Double.NEGATIVE_INFINITY);
            return (threshhold < 2*err) ? 0.0 : err;
        }
    }

    private static abstract class AbstractIterable<T>
            implements FuncyIterable<T> {
        
        @Override
        public <O,E extends Throwable>
                FuncyIterable<O> map(FuncE<O,? super T,E> f) throws E {
            return Iterables.map(this, Funcs.asFunc(f));
        }

        @Override
        public <E extends Throwable>
                FuncyIterable<T> subset(FuncE<Boolean,? super T,E> f)
                throws E {
            return Iterables.subset(this, Funcs.asFunc(f));
        }

        @Override
        public <O,C extends Collection<? super O>,E extends Throwable>
                C map(final FuncE<O,? super T,E> f, final C out) throws E {
            each( new FuncE<Void,T,E>() {
                public Void call(T d) throws E {
                    out.add( f.call(d) );
                    return null;
                }
            });
            return out;
        }

        @Override
        public <C extends Collection<? super T>,
                E extends Throwable>
                C subset(final FuncE<Boolean,? super T,E> f, final C out)
                throws E {
            each( new FuncE<Void,T,E>() {
                public Void call(T d) throws E {
                    if ( f.call(d) ) out.add(d);
                    return null;
                }
            });
            return out;
        }

        @Override
        public <E extends Throwable>
                boolean every(final FuncE<Boolean,? super T, E> f)
                throws E {
            try {
                each( new FuncE<Void,T,E>() {
                    public Void call(T d) throws E {
                        if ( !f.call(d) ) throw new IterationTerminator();
                        return null;
                    }
                });
            } catch (IterationTerminator t) {
                return false;
            }
            return true;
        }

        @Override
        public <E extends Throwable>
                boolean any(final FuncE<Boolean,? super T, E> f)
                throws E {
            try {
                each( new FuncE<Void,T,E>() {
                    public Void call(T d) throws E {
                        if ( f.call(d) ) throw new IterationTerminator();
                        return null;
                    }
                });
            } catch (IterationTerminator t) {
                return true;
            }
            return false;
        }

        private static class IterationTerminator extends RuntimeException {}
    }
            
    private static abstract class AbstractDoubleRange
            extends AbstractIterable<Double>
            implements Range<Double> {
        
        /**
         * The maxiumum consecutive integer that a double can represent (2^53).
         */
        static final double MAX_INT = 9.007199254740992e15;

        final double lower;
        final double upper;
        final double step;
        final FPDoubleErr err;
                
        AbstractDoubleRange(double lower, double upper, double step,
                            FPDoubleErr err) {
            this.lower = lower;
            this.upper = upper;
            this.step = step;
            this.err = err;
        }

        @Override
        public boolean contains(Double t) {
            if (t == null) {
                return false;
            } else {
                double d = t;
                return (d >= lower) && (d <= upper);
            }
        }

    }

    private static abstract class AbstractBigDoubleRange
            extends AbstractDoubleRange {

        private static final double ARBITRARY_LG_INT = Integer.MAX_VALUE;

        final double initialAdjust;
        final boolean initialCompute;

        AbstractBigDoubleRange(double lower, double upper, double step,
                               FPDoubleErr err) {
            super(lower, upper, step, err);

            // how many by is it going to take to reach the next number?
            double next = (step < 0) ?
                ceil( ulp(upper, -1.0) / abs(step) ) :
                ceil( ulp(lower, 1.0) / step );
            if (next > ARBITRARY_LG_INT) {
                initialAdjust = min(next, MAX_INT);
                initialCompute = false; // avoid some unnecessary calculation
            } else {
                initialAdjust = MAX_INT;
                initialCompute = true;
            }
        }

        /**
         * Finds the number of steps until the simple floating-point calculation
         * {@code (sofar + (i * step)} is no longer an accurate representation
         * (due to loss of precision).
         */
        double findRollover(BigCounter counter, double sofar) {
            double j = 0.0, k = MAX_INT;
            for (double i=ceil(k/2); i!=k; i=ceil(j+k/2)) {
                if ( counter.lookAhead(i) == (sofar + (i * step)) ) {
                    j = i;
                } else {
                    k = i;
                }
            }
            return k;
        }
    }
    
    static class AscendingInclusiveDoubleRange extends AbstractDoubleRange
            implements FloatingPointInclusiveRange<Double>,
                       FloatingPointIterable<Double> {
        
        private final double errMag;

        AscendingInclusiveDoubleRange(double lower, double upper) {
            this(lower, upper, 1.0, FPDoubleErrOf1Ulp.INSTANCE);
        }

        private AscendingInclusiveDoubleRange(double lower, double upper,
                                              double step, FPDoubleErr err) {
            super(lower, upper, step, err);
            errMag = err.errorUp(upper, step);
        }
        
        @Override
        public AscendingExclusiveDoubleRange exclusive() {
            return new AscendingExclusiveDoubleRange(lower, upper, step, err);
        }

        @Override
        public FloatingPointIterable<Double> by(Number selector) {
            return new AscendingInclusiveDoubleRange(lower, upper,
                                                 selector.doubleValue(), err);
        }

        @Override
        public FuncyIterable<Double> by(Step<Double> step) {
            throw new UnsupportedOperationException();
        }

        @Override
        public FuncyIterable<Double> within(Double nerr) {
            return new AscendingInclusiveDoubleRange(lower, upper,
                                                 step, FPDoubleErr.of(nerr) );
        }

        private boolean withinErr(double d, double i) {
            if (d <= upper+errMag) {
                if (d == Double.POSITIVE_INFINITY) {
                    double e = big(lower)
                            .add( big(step).multiply( big(i) ) )
                            .subtract( big(upper) ).doubleValue();
                    return (e <= errMag);
                }
                return true;
            }
            return false;
        }

        @Override
        public <E extends Throwable>
                void each(FuncE<?,? super Double, E> f) throws E {
            double i = 0.0, d = lower;
            for (; d<upper; d=lower+(++i*step)) {
                f.call(d);
            }
            if ( withinErr(d,i) )
                f.call(upper);
        }

        @Override
        public FuncyIterator<Double> iterator() {
            return new AbstractFuncyIterator<Double>() {

                double i = 0.0;
                double d = lower;
                boolean keepGoing = true;

                // naive Iterator
                // hasNext has a side effect!
                @Override public boolean hasNext() {
                    return (d < upper) || ( keepGoing && coerceNext() );
                }
                
                private boolean coerceNext() {
                    keepGoing = false;
                    if ( withinErr(d,i) ) {
                        d = upper;
                        return true;
                    }
                    return false;
                }

                @Override public Double next() {
                    Double next = d;
                    d = lower + (++i * step);
                    return next;
                }

                @Override public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }
    
    static class AscendingExclusiveDoubleRange extends AbstractDoubleRange
            implements FloatingPointClosedRange<Double>,
                       FloatingPointIterable<Double> {
        
        private final double limit;

        AscendingExclusiveDoubleRange(double lower, double upper,
                                      double step, FPDoubleErr err) {
            super(lower, nextDown(upper), step, err);
            limit = upper - err.errorDown(upper, step);
        }

        @Override
        public FloatingPointIterable<Double> by(Number selector) {
            return new AscendingExclusiveDoubleRange(lower, upper,
                                                 selector.doubleValue(), err);
        }

        @Override
        public FuncyIterable<Double> by(Step<Double> step) {
            throw new UnsupportedOperationException();
        }

        @Override
        public FuncyIterable<Double> within(Double nerr) {
            return new AscendingExclusiveDoubleRange(lower, upper,
                                                 step, FPDoubleErr.of(nerr) );
        }

        @Override
        public <E extends Throwable>
                void each(FuncE<?,? super Double, E> f) throws E {
            for (double i=0.0, d=lower; d<limit; d=lower+(++i*step)) {
                f.call(d);
            }
        }

        @Override
        public FuncyIterator<Double> iterator() {
            return new AbstractFuncyIterator<Double>() {

                double i = 0.0;
                double d = lower;

                @Override public boolean hasNext() {
                    return (d < limit);
                }

                @Override public Double next() {
                    Double next = d;
                    d = lower + (++i * step);
                    return next;
                }

                @Override public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }
    
    static class AscendingInclusiveBigDoubleRange
            extends AbstractBigDoubleRange
            implements FloatingPointInclusiveRange<Double>,
                       FloatingPointIterable<Double> {
        
        private final double errMag;

        AscendingInclusiveBigDoubleRange(double lower, double upper) {
            this(lower, upper, 1.0, FPDoubleErrOf1Ulp.INSTANCE);
        }

        private AscendingInclusiveBigDoubleRange(double lower, double upper,
                                                 double step, FPDoubleErr err) {
            super(lower, upper, step, err);
            errMag = err.errorUp(upper, step);
        }

        @Override
        public AscendingExclusiveBigDoubleRange exclusive() {
            return new AscendingExclusiveBigDoubleRange(lower, upper,
                                                        step, err);
        }

        @Override
        public FloatingPointIterable<Double> by(Number step) {
            return new AscendingInclusiveBigDoubleRange(lower, upper,
                                                     step.doubleValue(), err);
        }

        @Override
        public FuncyIterable<Double> by(Step<Double> step) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public FuncyIterable<Double> within(Double nerr) {
            return new AscendingInclusiveBigDoubleRange(lower, upper,
                                                 step, FPDoubleErr.of(nerr) );
        }
        
        private boolean withinErr(double d, BigCounter counter, double i) {
            if (d <= upper+errMag) {
                if (d == Double.POSITIVE_INFINITY) {
                    if (counter == null)
                        counter = new BigCounter(lower, step);
                    if (i != 0.0)
                        counter.increment(i);
                    return (counter.minus(upper) <= errMag);
                }
                return true;
            }
            return false;
        }

        @Override
        public <E extends Throwable>
                void each(FuncE<?,? super Double, E> f) throws E {

            if (lower > upper) return;

            double readjust = initialAdjust;
            boolean compute = initialCompute;
            BigCounter counter = null;

            Double Value;
            double value, sofar;            
            Value = value = sofar = lower;

            for (double i = 0.0; ; ) {

                f.call(Value);
                
                if (++i == readjust) { // readjust sofar when at readjust

                    if (counter == null)
                        counter = new BigCounter(lower, step);
                    Value = value = sofar = counter.increment(i);

                    // check for end of loop
                    // done here (and duplicated below)
                    // for the sake of efficiency in the main loop
                    // (number may not change on every iteration)
                    if (value >= upper) {
                        if ( withinErr(value, counter, 0.0) )
                            f.call(upper);
                        break;
                    }

                    i = 0.0;

                    // determine steps until next readjustment
                    if ( counter.lookAhead(MAX_INT)
                            != (sofar + (MAX_INT * step)) ) {
                        readjust = findRollover(counter, sofar);
                    } else {
                        readjust = MAX_INT;
                    }

                    // if the value will be the same until readjust
                    // don't compute it
                    compute = value != (sofar + ((readjust - 1.0) * step));
                                        
                } else if (compute) {

                    double v = sofar + (i * step);
                    if (v != value) {      // prevent the possible generation of
                        Value = value = v; // copious amounts of object garbage
                        
                        // check for end of loop
                        // done here (and duplicated above)
                        // for the sake of efficiency in the main loop
                        // (number may not change on every iteration)
                        if (value >= upper) {
                            if ( withinErr(value, counter, i) )
                                f.call(upper);
                            break;
                        }
                    }
                }
            }
        }

        @Override
        public FuncyIterator<Double> iterator() {
            return new AbstractFuncyIterator<Double>() {

                double i = 0.0;
                double readjust = initialAdjust;
                boolean compute = initialCompute;
                BigCounter counter = null;

                double sofar = lower;
                double value = sofar;
                Double Value = value;
                                
                boolean keepGoing = (lower <= upper);

                @Override public boolean hasNext() {
                    return keepGoing;
                }

                @Override public Double next() {
                    Double next = Value;
                    if (++i == readjust) { // readjust sofar when at readjust

                        // this check was placed here because it is rare
                        // that (++i == readjust) and thus should not have
                        // a significant impact on performance
                        if (value >= upper) {
                            keepGoing = false;
                            return next;
                        }

                        if (counter == null)
                            counter = new BigCounter(lower, step);
                        Value = value = sofar = counter.increment(i);

                        i = 0.0;

                        // check for end of iteration
                        // done here (and duplicated below)
                        // for the sake of efficiency
                        // (number may not change on every iteration)
                        if (value >= upper) {
                            if ( withinErr(value, counter, 0.0) ) {
                                Value = upper;
                                readjust = 1.0; // end iteration next
                            } else {
                                keepGoing = false; // end iteration now
                            }
                            return next;
                        }
                        
                        // determine steps until next readjustment
                        if ( counter.lookAhead(MAX_INT)
                                != (sofar + (MAX_INT * step)) ) {
                            readjust = findRollover(counter, sofar);
                        } else {
                            readjust = MAX_INT;
                        }

                        // if the value will be the same until readjust
                        // don't compute it
                        compute = value != (sofar + ((readjust - 1.0) * step));

                    } else if (compute) {

                        double v = sofar + (i * step);
                        if (v != value) {      // prevent object garbage
                            Value = value = v;

                            // check for end of iteration
                            // done here (and duplicated above)
                            // for the sake of efficiency
                            // (number may not change on every iteration)
                            if (value >= upper) {
                                if ( withinErr(value, counter, 0.0) ) {
                                    Value = upper;
                                    readjust = i + 1.0; // end iteration next
                                } else {
                                    keepGoing = false; // end after now
                                }
                            }
                        }
                    }
                    return next;
                }

                @Override public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }
    
    static class AscendingExclusiveBigDoubleRange
            extends AbstractBigDoubleRange
            implements FloatingPointClosedRange<Double>,
                       FloatingPointIterable<Double> {
        
        private final double limit;

        AscendingExclusiveBigDoubleRange(double lower, double upper,
                                         double step, FPDoubleErr err) {
            super(lower, nextDown(upper), step, err);
            limit = upper - err.errorDown(upper, step);
        }
        
        @Override
        public FloatingPointIterable<Double> by(Number step) {
            return new AscendingExclusiveBigDoubleRange(lower, upper,
                                                     step.doubleValue(), err);
        }

        @Override
        public FuncyIterable<Double> by(Step<Double> step) {
            throw new UnsupportedOperationException();
        }

        @Override
        public FuncyIterable<Double> within(Double nerr) {
            return new AscendingExclusiveBigDoubleRange(lower, upper,
                                                 step, FPDoubleErr.of(nerr) );
        }

        @Override
        public <E extends Throwable>
                void each(FuncE<?,? super Double, E> f) throws E {

            if (lower >= upper) return;

            double readjust = initialAdjust;
            boolean compute = initialCompute;
            BigCounter counter = null;

            Double Value;
            double value, sofar;            
            Value = value = sofar = lower;

            for (double i = 0.0; ; ) {

                f.call(Value);
                
                if (++i == readjust) { // readjust sofar when at readjust

                    if (counter == null)
                        counter = new BigCounter(lower, step);
                    Value = value = sofar = counter.increment(i);

                    // check for end of loop
                    // done here (and duplicated below)
                    // for the sake of efficiency in the main loop
                    // (number may not change on every iteration)
                    if (value >= limit) {
                        break;
                    }

                    i = 0.0;

                    // determine steps until next readjustment
                    if ( counter.lookAhead(MAX_INT)
                            != (sofar + (MAX_INT * step)) ) {
                        readjust = findRollover(counter, sofar);
                    } else {
                        readjust = MAX_INT;
                    }

                    // if the value will be the same until readjust
                    // don't compute it
                    compute = value != (sofar + ((readjust - 1.0) * step));
                                        
                } else if (compute) {

                    double v = sofar + (i * step);
                    if (v != value) {      // prevent the possible generation of
                        Value = value = v; // copious amounts of object garbage
                        
                        // check for end of loop
                        // done here (and duplicated above)
                        // for the sake of efficiency in the main loop
                        // (number may not change on every iteration)
                        if (value >= limit) {
                            break;
                        }
                    }
                }
            }
        }

        @Override
        public FuncyIterator<Double> iterator() {
            return new AbstractFuncyIterator<Double>() {

                double i = 0.0;
                double readjust = initialAdjust;
                boolean compute = initialCompute;
                BigCounter counter = null;

                double sofar = lower;
                double value = sofar;
                Double Value = value;
                                
                boolean keepGoing = (lower < upper);

                @Override public boolean hasNext() {
                    return keepGoing;
                }

                @Override public Double next() {
                    Double next = Value;
                    if (++i == readjust) { // readjust sofar when at readjust

                        if (counter == null)
                            counter = new BigCounter(lower, step);
                        Value = value = sofar = counter.increment(i);

                        i = 0.0;

                        // check for end of iteration
                        // done here (and duplicated below)
                        // for the sake of efficiency
                        // (number may not change on every iteration)
                        if (value >= limit) {
                            keepGoing = false;
                            return next;
                        }
                        
                        // determine steps until next readjustment
                        if ( counter.lookAhead(MAX_INT)
                                != (sofar + (MAX_INT * step)) ) {
                            readjust = findRollover(counter, sofar);
                        } else {
                            readjust = MAX_INT;
                        }

                        // if the value will be the same until readjust
                        // don't compute it
                        compute = value != (sofar + ((readjust - 1.0) * step));

                    } else if (compute) {

                        double v = sofar + (i * step);
                        if (v != value) {      // prevent object garbage
                            Value = value = v;

                            // check for end of iteration
                            // done here (and duplicated above)
                            // for the sake of efficiency
                            // (number may not change on every iteration)
                            if (value >= limit) {
                                keepGoing = false;
                            }
                        }
                    }
                    return next;
                }

                @Override public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }
    
    static class AscendingInfiniteDoubleRange
            extends AbstractBigDoubleRange
            implements FloatingPointInclusiveRange<Double>,
                       FloatingPointIterable<Double> {
        
        private final double errMag;

        AscendingInfiniteDoubleRange(double lower) {
            this(lower, Double.POSITIVE_INFINITY,
                    1.0, FPDoubleErrOf1Ulp.INSTANCE);
        }

        // either lower, upper or both must be -/Infinity
        private AscendingInfiniteDoubleRange(double lower, double upper,
                                             double step, FPDoubleErr err) {
            super(lower, upper, step, err);
            errMag = err.errorUp(Double.MAX_VALUE, step);
        }

        @Override
        public AscendingInfiniteDoubleRange exclusive() {
            return this;
        }

        @Override
        public FloatingPointIterable<Double> by(Number step) {
            return new AscendingInfiniteDoubleRange(lower, upper,
                                                    step.doubleValue(), err);
        }

        @Override
        public FuncyIterable<Double> by(Step<Double> step) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public FuncyIterable<Double> within(Double nerr) {
            return new AscendingInfiniteDoubleRange(lower, upper,
                                                 step, FPDoubleErr.of(nerr) );
        }
        
        @Override
        public <E extends Throwable>
                void each(FuncE<?,? super Double, E> f) throws E {

            if ( Double.isInfinite(lower) ) {
                f.call(lower);
                return;
            }

            double readjust = initialAdjust;
            boolean compute = initialCompute;
            BigCounter counter = null;

            Double Value;
            double value, sofar;            
            Value = value = sofar = lower;

            for (double i = 0.0; ; ) {

                f.call(Value);
                
                if (++i == readjust) { // readjust sofar when at readjust

                    if (counter == null)
                        counter = new BigCounter(lower, step);
                    Value = value = sofar = counter.increment(i);

                    // check for end of loop
                    // done here (and duplicated below)
                    // for the sake of efficiency in the main loop
                    // (number may not change on every iteration)
                    if (value == Double.POSITIVE_INFINITY) {
                        // include Double.MAX_VALUE if within margin of error
                        if ( ( step > Math.ulp(Double.MAX_VALUE) ) &&
                             (counter.minus(Double.MAX_VALUE) <= errMag) )
                            f.call(Double.MAX_VALUE);
                        f.call(Double.POSITIVE_INFINITY);
                        break;
                    }

                    i = 0.0;

                    // determine steps until next readjustment
                    if ( counter.lookAhead(MAX_INT)
                            != (sofar + (MAX_INT * step)) ) {
                        readjust = findRollover(counter, sofar);
                    } else {
                        readjust = MAX_INT;
                    }

                    // if the value will be the same until readjust
                    // don't compute it
                    compute = value != (sofar + ((readjust - 1.0) * step));
                                        
                } else if (compute) {

                    double v = sofar + (i * step);
                    if (v != value) {      // prevent the possible generation of
                        Value = value = v; // copious amounts of object garbage
                        
                        // check for end of loop
                        // done here (and duplicated above)
                        // for the sake of efficiency in the main loop
                        // (number may not change on every iteration)
                        if (value == Double.POSITIVE_INFINITY) {
                            // include Double.MAX_VALUE if w/in margin of error
                            if ( step > Math.ulp(Double.MAX_VALUE) ) {
                                if (counter == null)
                                    counter = new BigCounter(lower, step);
                                counter.increment(i);
                                if (counter.minus(Double.MAX_VALUE) <= errMag)
                                    f.call(Double.MAX_VALUE);
                            }
                            f.call(Double.POSITIVE_INFINITY);
                            break;
                        }
                    }
                }
            }
        }

        @Override
        public FuncyIterator<Double> iterator() {
            if ( Double.isInfinite(lower) ) {
                return new AbstractFuncyIterator<Double>() {

                    boolean keepGoing = true;

                    @Override
                    public boolean hasNext() {
                        return keepGoing;
                    }

                    @Override
                    public Double next() {
                        keepGoing = false;
                        return lower;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
            return new AbstractFuncyIterator<Double>() {

                double i = 0.0;
                double readjust = initialAdjust;
                boolean compute = initialCompute;
                BigCounter counter = null;

                double sofar = lower;
                double value = sofar;
                Double Value = value;

                boolean keepGoing = true;

                @Override public boolean hasNext() {
                    return keepGoing;
                }

                @Override public Double next() {
                    Double next = Value;
                    if (++i == readjust) { // readjust sofar when at readjust

                        // this check was placed here because it is rare
                        // that (++i == readjust) and thus should not have
                        // a significant impact on performance
                        if (value == Double.POSITIVE_INFINITY) {
                            keepGoing = false;
                            return next;
                        }
                        
                        if (counter == null)
                            counter = new BigCounter(lower, step);
                        Value = value = sofar = counter.increment(i);

                        i = 0.0;

                        // check for end of iteration
                        // done here (and duplicated below)
                        // for the sake of efficiency
                        // (number may not change on every iteration)
                        if (value == Double.POSITIVE_INFINITY) {
                            // include Double.MAX_VALUE if w/in margin of error
                            if (next != Double.MAX_VALUE &&
                                    counter.minus(Double.MAX_VALUE) <= errMag)
                                Value = Double.MAX_VALUE;
                            readjust = 1.0;
                            return next;
                        }
                        
                        // determine steps until next readjustment
                        if ( counter.lookAhead(MAX_INT)
                                != (sofar + (MAX_INT * step)) ) {
                            readjust = findRollover(counter, sofar);
                        } else {
                            readjust = MAX_INT;
                        }

                        // if the value will be the same until readjust
                        // don't compute it
                        compute = value != (sofar + ((readjust - 1.0) * step));

                    } else if (compute) {

                        double v = sofar + (i * step);
                        if (v != value) {      // prevent object garbage
                            Value = value = v;

                            // check for end of iteration
                            // done here (and duplicated above)
                            // for the sake of efficiency
                            // (number may not change on every iteration)
                            if (value == Double.POSITIVE_INFINITY) {
                                // include Double.MAX_VALUE if w/in error
                                if (next != Double.MAX_VALUE &&
                                      counter.minus(Double.MAX_VALUE) <= errMag)
                                    Value = Double.MAX_VALUE;
                                readjust = 1.0;
                            }
                        }
                    }
                    return next;
                }

                @Override public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }
}

