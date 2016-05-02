package org.andrill.visualizer.app.cores.dataset;

/**
 * A simple class to represent a range with a value at the top and bottom.
 * 
 * @author Josh Reed (jareed@andrill.org)
 * 
 * @param <E>
 *            the value Class.
 */
public class Range<E extends Comparable<? super E>> implements Comparable<Range<E>> {
	public double end;
	public E endValue;
	public double start;
	public E startValue;

	/**
	 * Create a new Range.
	 */
	public Range() {
	}

	/**
	 * Create a new Range.
	 * 
	 * @param start
	 *            the start.
	 * @param startValue
	 *            the start value.
	 * @param end
	 *            the end.
	 * @param endValue
	 *            the end value.
	 */
	public Range(final double start, final E startValue, final double end, final E endValue) {
		this.start = start;
		this.end = end;
		this.startValue = startValue;
		this.endValue = endValue;
	}

	/**
	 * {@inheritDoc}
	 */
	public int compareTo(final Range<E> o) {
		if (start < o.start) {
			return -1;
		} else if (start > o.start) {
			return 1;
		} else {
			if (end < o.end) {
				return -1;
			} else if (end > o.end) {
				return 1;
			} else {
				return 0;
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Range<E> other = (Range) obj;
		if (Double.doubleToLongBits(end) != Double.doubleToLongBits(other.end)) {
			return false;
		}
		if (endValue == null) {
			if (other.endValue != null) {
				return false;
			}
		} else if (endValue.compareTo(other.endValue) != 0) {
			return false;
		}
		if (Double.doubleToLongBits(start) != Double.doubleToLongBits(other.start)) {
			return false;
		}
		if (startValue == null) {
			if (other.startValue != null) {
				return false;
			}
		} else if (startValue.compareTo(other.startValue) != 0) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(end);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((endValue == null) ? 0 : endValue.hashCode());
		temp = Double.doubleToLongBits(start);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((startValue == null) ? 0 : startValue.hashCode());
		return result;
	}
}

