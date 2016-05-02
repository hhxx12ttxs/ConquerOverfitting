<<<<<<< HEAD
/*
 * tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package alice.tuprolog;

import java.util.List;

/**
 *
 * Double class represents the double prolog data type
 *
 */
@SuppressWarnings("serial")
public class Double extends Number {
    
    private double value;
    
    public Double(double v) {
        value = v;
    }
    
    /**
     *  Returns the value of the Double as int
     */
    final public int intValue() {
        return (int) value;
    }
    
    /**
     *  Returns the value of the Double as float
     *
     */
    final public float floatValue() {
        return (float) value;
    }
    
    /**
     *  Returns the value of the Double as double
     *
     */
    final public double doubleValue() {
        return value;
    }
    
    /**
     *  Returns the value of the Double as long
     */
    final public long longValue() {
        return (long) value;
    }
    
    
    /** is this term a prolog integer term? */
    final public boolean isInteger() {
        return false;
    }
    
    /** is this term a prolog real term? */
    final public boolean isReal() {
        return true;
    }
    
    /** is an int Integer number? 
     * @deprecated Use <tt>instanceof Int</tt> instead. */
    final public boolean isTypeInt() {
        return false;
    }

    /** is an int Integer number?
     * @deprecated Use <tt>instanceof Int</tt> instead. */
    final public boolean isInt() {
        return false;
    }
    
    /** is a float Real number? 
     * @deprecated Use <tt>instanceof alice.tuprolog.Float</tt> instead. */
    final public boolean isTypeFloat() {
        return false;
    }

    /** is a float Real number?
     * @deprecated Use <tt>instanceof alice.tuprolog.Float</tt> instead. */
    final public boolean isFloat() {
        return false;
    }
    
    /** is a double Real number? 
     * @deprecated Use <tt>instanceof alice.tuprolog.Double</tt> instead. */
    final public boolean isTypeDouble() {
        return true;
    }

    /** is a double Real number?
     * @deprecated Use <tt>instanceof alice.tuprolog.Double</tt> instead. */
    final public boolean isDouble() {
        return true;
    }
    
    /** is a long Integer number? 
     * @deprecated Use <tt>instanceof alice.tuprolog.Long</tt> instead. */
    final public boolean isTypeLong() {
        return false;
    }

    /** is a long Integer number?
     * @deprecated Use <tt>instanceof alice.tuprolog.Long</tt> instead. */
    final public boolean isLong() {
        return false;
    }
    
    /**
     * Returns true if this Double term is grater that the term provided.
     * For number term argument, the int value is considered.
     */
    public boolean isGreater(Term t) {
        t = t.getTerm();
        if (t instanceof Number) {
            return value>((Number)t).doubleValue();
        } else if (t instanceof Struct) {
            return false;
        } else if (t instanceof Var) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Returns true if this Double term is equal to the term provided.
     */
    public boolean isEqual(Term t) {
        t = t.getTerm();
        if (t instanceof Number) {
            Number n = (Number) t;
            if (!n.isReal())
                return false;
            return value == n.doubleValue();
        } else {
            return false;
        }
    }
    
    /**
     * Tries to unify a term with the provided term argument.
     * This service is to be used in demonstration context.
     */
    boolean unify(List<Var> vl1, List<Var> vl2, Term t) {
        t = t.getTerm();
        if (t instanceof Var) {
            return t.unify(vl2, vl1, this);
        } else if (t instanceof Number && ((Number) t).isReal()) {
            return value == ((Number) t).doubleValue();
        } else {
            return false;
        }
    }
    
    public String toString() {
        return java.lang.Double.toString(value);
    }
    
    public int resolveVariables(int count) {
        return count;
    }

    /**
     * @author Paolo Contessi
     */
    public int compareTo(Number o) {
        return (new java.lang.Double(value)).compareTo(o.doubleValue());
    }
    
}
=======
package se.l4.vibe.probes;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Range operations for {@link TimeSeries time series}.
 * 
 * @author Andreas Holstenson
 *
 */
public class Range
{
	private Range()
	{
	}
	
	/**
	 * Return a probe that will always return the minimum value ever measured
	 * in the given series.
	 * 
	 * @param series
	 * @return
	 */
	public static <T extends Number> Probe<Double> min(TimeSeries<T> series)
	{
		return new SeriesMinMax<T>(series, ValueReaders.<T>same(), true);
	}
	
	/**
	 * Return a probe that will always return the minimum value ever measured
	 * in the given series.
	 * 
	 * @param series
	 * @return
	 */
	public static <T, N extends Number> Probe<Double> min(TimeSeries<T> series, ValueReader<T, N> reader)
	{
		return new SeriesMinMax<T>(series, reader, true);
	}
	
	/**
	 * Return a probe that will always return the maximum value ever measured
	 * in the given series.
	 * 
	 * @param series
	 * @return
	 */
	public static <T, N extends Number> Probe<Double> max(TimeSeries<T> series, ValueReader<T, N> reader)
	{
		return new SeriesMinMax<T>(series, reader, false);
	}
	
	/**
	 * Return a probe that will return the minimum value measured over a
	 * certain period.
	 * 
	 * @param series
	 * @return
	 */
	public static <T extends Number> Probe<Double> minimum(
			TimeSeries<T> series,
			long duration,
			TimeUnit unit)
	{
		return TimeSeriesProbes.forSeries(series, duration, unit, new MinOperation<T, T>(ValueReaders.<T>same()));
	}
	
	/**
	 * Return a probe that will return the minimum value measured over a
	 * certain period.
	 * 
	 * @param series
	 * @return
	 */
	public static <T, N extends Number> Probe<Double> minimum(
			TimeSeries<T> series,
			ValueReader<T, N> reader,
			long duration,
			TimeUnit unit)
	{
		return TimeSeriesProbes.forSeries(series, duration, unit, new MinOperation<T, N>(reader));
	}
	
	/**
	 * Return a probe that will return the maximum value measured over a
	 * certain period.
	 * 
	 * @param series
	 * @return
	 */
	public static <T extends Number> Probe<Double> maximum(
			TimeSeries<T> series,
			long duration,
			TimeUnit unit)
	{
		return TimeSeriesProbes.forSeries(series, duration, unit, new MaxOperation<T, T>(ValueReaders.<T>same()));
	}
	
	/**
	 * Return a probe that will return the maximum value measured over a
	 * certain period.
	 * 
	 * @param series
	 * @return
	 */
	public static <T, N extends Number> Probe<Double> maximum(
			TimeSeries<T> series,
			ValueReader<T, N> reader,
			long duration,
			TimeUnit unit)
	{
		return TimeSeriesProbes.forSeries(series, duration, unit, new MaxOperation<T, N>(reader));
	}
	
	/**
	 * Create a new operation that will calculate the minimum of any time
	 * series.
	 * 
	 * @return
	 */
	public static <T extends Number> TimeSeriesOperation<T, Double> newMinimumOperation()
	{
		return new MinOperation<T, T>(ValueReaders.<T>same());
	}

	/**
	 * Create a new operation that will calculate the minimum of any time
	 * series.
	 * 
	 * @return
	 */
	public static <T, N extends Number> TimeSeriesOperation<T, Double> newMinimumOperation(ValueReader<T, N> reader)
	{
		return new MinOperation<T, N>(reader);
	}
	
	/**
	 * Create a new operation that will calculate the minimum of any time
	 * series.
	 * 
	 * @return
	 */
	public static <T extends Number> TimeSeriesOperation<T, Double> newMaximumOperation()
	{
		return new MaxOperation<T, T>(ValueReaders.<T>same());
	}
	
	/**
	 * Create a new operation that will calculate the minimum of any time
	 * series.
	 * 
	 * @return
	 */
	public static <T, N extends Number> TimeSeriesOperation<T, Double> newMaximumOperation(ValueReader<T, N> reader)
	{
		return new MaxOperation<T, N>(reader);
	}
	
	private static class MinOperation<I, O extends Number>
		implements TimeSeriesOperation<I, Double>
	{
		private final ValueReader<I, O> reader;
		private double value;
		
		public MinOperation(ValueReader<I, O> reader)
		{
			this.reader = reader;
		}

		@Override
		public void add(I value, Collection<TimeSeries.Entry<I>> entries)
		{
			double min = Double.MAX_VALUE;
			for(TimeSeries.Entry<I> entry : entries)
			{
				min = Math.min(min, reader.read(entry.getValue()).doubleValue());
			}
			
			this.value = min;
		}
		
		@Override
		public void remove(I value, Collection<TimeSeries.Entry<I>> entries)
		{
			// Do nothing
		}
		
		@Override
		public Double get()
		{
			return value;
		}
	}
	
	private static class MaxOperation<I, T extends Number>
		implements TimeSeriesOperation<I, Double>
	{
		private final ValueReader<I, T> reader;
		private double value;
	
		public MaxOperation(ValueReader<I, T> reader)
		{
			this.reader = reader;
		}
		
		@Override
		public void add(I value, Collection<TimeSeries.Entry<I>> entries)
		{
			double max = Double.MIN_VALUE;
			for(TimeSeries.Entry<I> entry : entries)
			{
				max = Math.max(max, reader.read(entry.getValue()).doubleValue());
			}
			
			this.value = max;
		}
		
		@Override
		public void remove(I value, Collection<TimeSeries.Entry<I>> entries)
		{
			// Do nothing
		}
		
		@Override
		public Double get()
		{
			return value;
		}
	}
	
	private static class SeriesMinMax<T>
		implements Probe<Double>
	{
		private double value;
		
		public SeriesMinMax(TimeSeries<T> series, final ValueReader<T, ? extends Number> reader, final boolean min)
		{
			value = min ? Double.MAX_VALUE : Double.MIN_NORMAL;
			series.addListener(new SampleListener<T>()
			{
				@Override
				public void sampleAcquired(SampledProbe<T> probe, TimeSeries.Entry<T> entry)
				{
					double newValue = reader.read(entry.getValue()).doubleValue();
					if(min)
					{
						value = Math.min(newValue, value);
					}
					else
					{
						value = Math.max(newValue, value);
					}
				}
			});
		}
		
		@Override
		public Double read()
		{
			return value;
		}
	}
}

>>>>>>> 76aa07461566a5976980e6696204781271955163
