package algorithm.util;

import java.util.Arrays;
import java.util.BitSet;

final public class BasicUtils {

	static BitSet generateCompositeNumbersSet(int limit) {
		BitSet isComposite = new BitSet(limit+1);
		isComposite.set(0); isComposite.set(1);
		for (int i = 2, limitSqrt = 1 + (int)Math.sqrt(limit); i <= limitSqrt; i = isComposite.nextClearBit(i+1)) {
			for (int j = i*i; j <= limit; j+=i) {
				isComposite.set(j);
			}
		}
		return isComposite;
	}
	
	static int[] generatePrimes(int limit) {
		BitSet isComposite = generateCompositeNumbersSet(limit);
		int[] primes = new int[limit - isComposite.cardinality() + 1];
		for (int i = isComposite.nextClearBit(0), j = 0; i <= limit; i = isComposite.nextClearBit(i+1)) primes[j++] = i;
		return primes;
	}

	public static int gcd(int a, int b) {
		return (a > b) ? gcd(b, a) : (a > 0) ? gcd(b%a, a) : b;
	}
	
	public static long gcd(long a, long b) {
		return (a > b) ? gcd(b, a) : (a > 0) ? gcd(b%a, a) : b;
	}
	
	public static int[][] reIndex(int[] a) {
		// - array mapping
		int[] mapping = Arrays.copyOf(a, a.length);
		Arrays.sort(mapping);
		int i = 1;
		while(i < mapping.length && mapping[i-1] < mapping[i]) i++;
		int j = i;
		while (++i < a.length)
			if (mapping[i-1] < mapping[i])
				mapping[j++] = mapping[i];
		mapping = Arrays.copyOf(mapping, j);
		// - re-indexed array
		int[] array = new int[a.length];
		for (i = 0; i < a.length; i++) array[i] = Arrays.binarySearch(mapping, a[i]);
		return new int[][] {array, mapping};
	}
	
	public static long[][] reIndex(long[] a) {
		// - array mapping
		long[] mapping = Arrays.copyOf(a, a.length);
		Arrays.sort(mapping);
		int i = 1;
		while(i < mapping.length && mapping[i-1] < mapping[i]) i++;
		int j = i;
		while (++i < a.length)
			if (mapping[i-1] < mapping[i])
				mapping[j++] = mapping[i];
		mapping = Arrays.copyOf(mapping, j);
		// - re-indexed array
		long[] array = new long[a.length];
		for (i = 0; i < a.length; i++) array[i] = Arrays.binarySearch(mapping, a[i]);
		return new long[][] {array, mapping};
	}
	
	private BasicUtils() {};
}

