// Copyright 2010 Ray Ortigas. All rights reserved.
// <ray(dot)ortigas(at)gmail(dot)com>

package everyhand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class Combinatorics {

	static final class CombinationsIterator implements Iterator<Long> {
		private int n;
		private int k;
		private long next;

		public CombinationsIterator(int n, int k) {
			this.n = n;
			this.k = k;
			next = (1L << k) - 1;
		}

		public boolean hasNext() {
			return (next & (1L << n)) == 0;
		}

		public Long next() {
			// Gosper's hack, described by Knuth, referenced in
			// http://en.wikipedia.org/wiki/Combinatorial_number_system#Applications
			long result = next;
			long x = next;
			long u = x & -x;
			long v = u + x;
			x = v + (((v ^ x) / u) >> 2);
			next = x;
			return result;
		}

		public void remove() { throw new UnsupportedOperationException(); }
	}
	
	static final class PermutationsIterator implements Iterator<List<Integer>> {
		int n;
		int k;
		int nPk;
		List<Integer> elements;
		int i;
		List<Integer> next;
		
		public PermutationsIterator(int n, int k) {
			this.n = n;
			this.k = k;
			
			nPk = permute(n, k);
			
			List<Integer> elements = new ArrayList<Integer>();
			for (int i = 0; i < n; i++)
				elements.add(i);
			this.elements = Collections.unmodifiableList(elements);
		}

		public boolean hasNext() { return i < nPk; }

		public List<Integer> next() {
			List<Integer> next = new ArrayList<Integer>();
			List<Integer> notNext = new ArrayList<Integer>(elements);
			int r = i;
			int np = nPk;
			for (int j = 0; j < k; j++) {
				np /= n - j;
				next.add(notNext.remove(r / np));
				r %= np;
			}
			i++;
			return next;
		}

		public void remove() { throw new UnsupportedOperationException(); }
	}
	
	public static long toCombination(List<Integer> permutation) {
		long combination = 0;
		for (int i : permutation)
			combination |= (1L << i);
		return combination;
	}
	
	public static List<Integer> toPermutation(long combination) {
		List<Integer> permutation = new ArrayList<Integer>();
		long combinationRemaining = combination;
		int i = 0;
		while (combinationRemaining > 0) {
			if ((combinationRemaining & 1) > 0) {
				permutation.add(i);
			}
			combinationRemaining >>= 1;
			i++;
		}
		return permutation;
	}
	
	public static SortedMap<Integer, Integer> multiplicitiesOf(List<Integer> multiset) {
		SortedMap<Integer, Integer> multiplicities = new TreeMap<Integer, Integer>();
		for (Integer k : multiset) {
			Integer v = multiplicities.get(k);
			v = (v == null) ? 1 : (v + 1);
			multiplicities.put(k, v);
		}
		return multiplicities;
	}

	public static Iterator<Long> combinationsIterator(int n, int k) {
		return new CombinationsIterator(n, k);
	}
	
	public static Iterator<List<Integer>> permutationsIterator(int n, int k) {
		return new PermutationsIterator(n, k);
	}
	
	public static int factorial(int n) {
		int result = 1;
		for (int i = 1; i <= n; i++)
			result *= i;
		return result;
	}
	
	public static int permute(int n, int k) {
		int result = 1;
		for (int i = n - k + 1; i <= n; i++)
			result *= i;
		return result;
	}
	
	public static int choose(int n, int k) {
		return permute(n, k) / factorial(k);
	}

	public static void main(String[] args) {
		for (Iterator<Long> it = combinationsIterator(10, 3); it.hasNext(); ) {
			long next = it.next();
			System.out.format("%d\t%10s\n", next, Long.toBinaryString(next));
		}
		for (Iterator<List<Integer>> it = permutationsIterator(10, 3); it.hasNext(); ) {
			List<Integer> next = it.next();
			StringBuilder builder = new StringBuilder();
			int n = next.size();
			if (n > 0) {
				builder.append(next.get(0));
				for (int i = 1; i < n; i++) {
					builder.append(", ");
					builder.append(next.get(i));
				}
			}
			System.out.println(builder.toString());
		}
	}
}

