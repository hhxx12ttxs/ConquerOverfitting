package org.andrill.visualizer.app.cores.dataset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A list of ranges.
 * 
 * @author Josh Reed (jareed@andrill.org)
 * 
 * @param <E>
 *            the value Class.
 */
public class RangeList<E extends Comparable<? super E>> {
	private List<Range<E>> ranges;

	/**
	 * Create a new RangeList.
	 */
	public RangeList() {
		ranges = new ArrayList<Range<E>>();
	}

	/**
	 * Split a range at the specified depth.
	 * 
	 * @param depth
	 *            the depth.
	 * @param value
	 *            the value.
	 */
	public void add(final double depth, final E value) {
		final List<Range<E>> toAdd = new ArrayList<Range<E>>();
		for (final Iterator<Range<E>> rangeIter = ranges.iterator(); rangeIter.hasNext();) {
			final Range<E> r = rangeIter.next();
			if ((depth <= r.start) && (depth >= r.end)) {
				rangeIter.remove();
				toAdd.add(new Range<E>(r.start, r.startValue, depth, value));
				toAdd.add(new Range<E>(depth, value, r.end, r.endValue));
			}
		}
		ranges.addAll(toAdd);
		Collections.sort(ranges);
	}

	/**
	 * Add a new range to the list.
	 * 
	 * @param r
	 *            the range to add.
	 */
	public void add(final Range<E> add, final boolean allowOverlap) {
		final List<Range<E>> toAdd = new ArrayList<Range<E>>();
		for (final Iterator<Range<E>> rangeIter = ranges.iterator(); rangeIter.hasNext();) {
			final Range<E> r = rangeIter.next();
			if ((add.start <= r.start) && (add.end >= r.end)) {
				// completely overlaps, so replace
				rangeIter.remove();
			} else if ((add.start > r.start) && (add.end < r.end)) {
				// completely contained, so split
				rangeIter.remove();
				toAdd.add(new Range<E>(r.start, r.startValue, add.start, r.startValue));
				toAdd.add(new Range<E>(add.end, r.endValue, r.end, r.endValue));
			} else if ((add.start > r.start) && (add.start < r.end)) {
				// overlaps the bottom
				if (!allowOverlap) {
					add.end = r.end;
				}
				r.end = add.start;
			} else if ((add.end > r.start) && (add.end < r.end)) {
				// overlaps the top
				if (!allowOverlap) {
					add.start = r.start;
				}
				r.start = add.end;
			}
		}
		toAdd.add(add);
		ranges.addAll(toAdd);
		Collections.sort(ranges);
	}

	/**
	 * Add the range list to this range list.
	 * 
	 * @param list
	 *            the range list.
	 */
	public void add(final RangeList<E> list, final boolean allowOverlap) {
		for (final Range<E> r : list.getRanges()) {
			final Range<E> clone = new Range<E>(r.start, r.startValue, r.end, r.endValue);
			add(clone, allowOverlap);
		}
	}

	/**
	 * Get a DepthRangeDataSet from this list.
	 * 
	 * @param start
	 *            a clip to start value flag.
	 * @return the DepthRangeDataSet.
	 */
	public DepthRangeDataSet<E> getRangeDataSet(final boolean start) {
		final DepthRangeDataSet<E> data = new DepthRangeDataSet<E>();
		for (final Range<E> r : ranges) {
			data.add(new DepthRangeDatum<E>(r.start, r.end, (start ? r.startValue : r.endValue)));
		}
		return data;
	}

	/**
	 * Get the ranges in this list.
	 * 
	 * @return the ranges.
	 */
	public List<Range<E>> getRanges() {
		return Collections.unmodifiableList(ranges);
	}

	/**
	 * Get a DepthValueDataSet from this list.
	 * 
	 * @return the DepthValueDataSet.
	 */
	public DepthValueDataSet<E> getValueDataSet() {
		final DepthValueDataSet<E> data = new DepthValueDataSet<E>();
		for (final Range<E> r : ranges) {
			data.add(new DepthValueDatum<E>(r.start, r.startValue));
			data.add(new DepthValueDatum<E>(r.end, r.endValue));
		}
		return data;
	}
}

