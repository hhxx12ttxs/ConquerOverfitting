package tinydb.jointree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Subsets {

	public static <T> List<T> integerOrderSubset(int n, List<T> set) {
		final List<T> subset = new ArrayList<T>();

		int iMax = (int) Math.pow(2, set.size()) - 1;
		if (n > iMax) {
			throw new IllegalArgumentException();
		}

		int i = 0;
		while (i < iMax) {
			if ((1 & n) == 1) {
				subset.add(set.get(i));
			}
			n = n >> 1;
			i++;
		}

		return subset;
	}

	public static <T> Iterable<List<T>> subsetIterator(final List<T> set) {
		return new Iterable<List<T>>() {

			@Override
			public Iterator<List<T>> iterator() {
				return new Iterator<List<T>>() {

					private int i = 0;
					private int iMax = (int) Math.pow(2, set.size()) - 1;

					@Override
					public boolean hasNext() {
						return i < iMax;
					}

					@Override
					public List<T> next() {
						i++;
						return integerOrderSubset(i, set);
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};

			}
		};
	}

}

