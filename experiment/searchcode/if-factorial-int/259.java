package tinydb.jointree;

import java.util.BitSet;

public class Utils {
	
	/**
	 * Computes the n-th catalan number.
	 */
	public static long catalanNumber(long n) {
		return binom(2*n, n) / (n + 1);
	}
	
	/**
	 * Computes (iteratively) the factorial of n.
	 */
	public static long factorial(long n) {
		long factorial = n;
		for (long i = n; i > 1; i--) {
			factorial *= i;
		}
		return factorial;
	}
	
	/**
	 * Computes n choose k (efficiently).
	 */
	public static long binom(long n, long k) {
		if (k == 0) {
			return 1;
		}
		if (2*k > n) {
			return binom(n, n - k);
		}
		long result = n - k + 1;
		for (int i = 2; i <= k; i++) {
			result = result * (n - k + i);
			result = result / i;
		}
		return result;
	}
	
	/**
	 * Computes the number of different paths from (0,0) to (i,j).
	 */
	public static long p(long i, long j) {
		long n = i + 1;
		long k = (i + j) / 2 + 1;
		final long binom = binom(n, k);
		return (long) (((double)j + 1) / ((double)i + 1) * binom) ;
	}

	/**
	 * Computes the number of paths from (i,j) to (2n,0)
	 */
	public static long numberOfPaths(long i, long j, long numberOfInnerNodes) {
		if ((i + j) % 2 == 1) {
			return 0;
		}
		if (j > i) {
			return 0;
		}
		if (i > numberOfInnerNodes && j > numberOfInnerNodes-(i-numberOfInnerNodes)) {
			return 0;
		}
		return p(2 * numberOfInnerNodes - i, j);
	}
	
	/**
	 * @param numberOfInnerNodes a number of inner node
	 * @param rank a rank in [0, catalanNumber(n)[
	 * @return encoding of the inner nodes of a tree
	 */
	public static BitSet unrankTree(long numberOfInnerNodes, long rank) {
		System.out.println("Unranking tree " + rank + " with " + numberOfInnerNodes + " inner nodes.");
		int open = 1;
		int close = 0;
		int pos = 0;
		final BitSet encoding = new BitSet();
//		encoding.set(0);
		while (encoding.cardinality() < numberOfInnerNodes) {
			long k = numberOfPaths(open + close, open - close, numberOfInnerNodes);
//			System.out.println("open = " + open + ", close = " + close + ", k = " + k + ", r = " + rank + ", bits set = " + encoding);
			if (k <= rank) {
				rank = rank - k;
//				System.out.println("appending )");
				close++;
			} else {
//				System.out.println("appending (");
				encoding.set(pos);
				open++;
			}
			pos++;
		}
		System.out.println("Bits set: " + encoding);
		return encoding;
	}
	
	public static String dyckWord(BitSet encoding) {
		final int numberOfInnerNodes = encoding.cardinality();
		final StringBuilder stringEncoding = new StringBuilder();
		for (int i = 0; i < numberOfInnerNodes * 2; i++) {
			if (encoding.get(i)) {
				stringEncoding.append("(");
			} else {
				stringEncoding.append(")");
			}
		}
		return stringEncoding.toString();
	}
	
	public static int[] unrank(int numberOfElements, int rank) {
		int[] pi = new int[numberOfElements];
		for (int i = 0; i < numberOfElements; i++) {
			pi[i] = i;
		}
		for (int i = numberOfElements; i > 0; i--) {
			int tmp = pi[i-1];
			pi[i-1] = pi[rank % i];
			pi[rank % i] = tmp;
			rank = rank / i;
		}
		return pi;
	}

	public static double costCout(JoinTree t) {
		//C_out
		if (t.isLeafNode()) {
			return 0.0;
		} else {
			return t.getCardinality() + costCout(t.getLeftSubTree()) + costCout(t.getRightSubTree()); 
		}
	}
	
}

