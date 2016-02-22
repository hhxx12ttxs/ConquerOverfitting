package ch.schorn.numbertheory;

/*	This package defines an applet and an application to decompose a non-negative
	integer into a sum of at most four squares.
*/

/*	use tabstops 4

	Copyright (c) 2004 - 2005, Peter Schorn

	Permission is hereby granted, free of charge, to any person obtaining a
	copy of this software and associated documentation files (the "Software"),
	to deal in the Software without restriction, including without limitation
	the rights to use, copy, modify, merge, publish, distribute, sublicense,
	and/or sell copies of the Software, and to permit persons to whom the
	Software is furnished to do so, subject to the following conditions:

	The above copyright notice and this permission notice shall be included in
	all copies or substantial portions of the Software.

	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
	PETER SCHORN BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
	IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
	CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

	Change history
	29-Nov-2005 Peter Schorn
	-	Added isProbableSquare

	26-Nov-2005 Peter Schorn
	-	Simplified decomposePrime

	19-Nov-2005 Peter Schorn
	-	Replaced decomposePrime with version without isqrt

	12-Nov-2005 Peter Schorn
	-	Replaced isqrt by a more efficient version

	12-Dec-2004 Peter Schorn
	-	Introduced check for squares in iunit to handle (unlikely) case that a
		probable prime is not only composite but even a square resulting in a
		potentially extreme running time for iunit
	-	Fixed a bug in jacobi where instead of division by two multiplication
		by two was performed in a reduction step. No impact on final result as
		a subsequent step performed the correct division.
	-	Simplified the search loop in decompose and fixed a bug where in the case
		of an unsuccessful decomposition of a probable prime the algorithm would
		loop infinitely. Bug was unlikely to occur as isProbablePrime is very
		unlikely to identify a composite number as prime.
*/

import java.applet.Applet;
import java.awt.Button;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Label;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionListener;
import java.math.BigInteger;
import java.util.StringTokenizer;
import java.util.Vector;

public class FourSquares extends Applet { // this applet is compatible with JDK 1.1

	// version
	private final static String VERSION = "(V2.08 29-Nov-2005)";

	// Some BigInteger constants - ZERO and ONE only present since JDK 1.2
	private final static BigInteger ZERO		= BigInteger.valueOf(0L);
	private final static BigInteger ONE			= BigInteger.valueOf(1L);
	private final static BigInteger TWO			= BigInteger.valueOf(2L);
	private final static BigInteger MAXINT		= BigInteger.valueOf(Integer.MAX_VALUE);
	private final static BigInteger ITERBETTER	= ONE.shiftLeft(1024);

	// Certainty used for finding good primes
	private final static int primeCertainty = 10;

	/*	Special cases for the decomposition algorithm
		0 <= i < specialCasesArray.length = specialCasesDecomposition.length: specialCasesArray[i] =
		specialCasesDecomposition[i][0]^2 + specialCasesDecomposition[i][1]^2 +
		specialCasesDecomposition[i][2]^2
	*/
	private final static BigInteger[] specialCasesArray = new BigInteger[]{
		BigInteger.valueOf(9634L),	BigInteger.valueOf(2986L),	BigInteger.valueOf(1906L),
		BigInteger.valueOf(1414L),	BigInteger.valueOf(730L),	BigInteger.valueOf(706L),
		BigInteger.valueOf(526L),	BigInteger.valueOf(370L),	BigInteger.valueOf(226L),
		BigInteger.valueOf(214L),	BigInteger.valueOf(130L),	BigInteger.valueOf(85L),
		BigInteger.valueOf(58L),	BigInteger.valueOf(34L),	BigInteger.valueOf(10L),
		BigInteger.valueOf(3L),		TWO
	};
	private final static int[][] specialCasesDecomposition = new int[][]{
		{56,	57, 57},	{21,	32, 39},	{13,	21, 36},
		{ 6,	17, 33},	{ 0,	1,	27},	{15,	15, 16},
		{ 6,	7,	21},	{ 8,	9,	15},	{ 8,	9,	9},
		{ 3,	6,	13},	{ 0,	3,	11},	{ 0,	6,	7},
		{ 0,	3,	7},		{ 3,	3,	4},		{ 0,	1,	3},
		{ 1,	1,	1},		{ 0,	1,	1}
	};

	private final static java.util.Hashtable specialCases = new java.util.Hashtable(50);
	static {
		for (int i = 0; i < specialCasesArray.length; i++)
			specialCases.put(specialCasesArray[i], specialCasesDecomposition[i]);
	}

	private final static long magicN = 10080;
	private final static BigInteger bigMagicN = BigInteger.valueOf(magicN);
	// There are exactly 336 squares mod 10080. This is the smallest rate of squares
	// for all n < 15840.
	private final static java.util.Hashtable squaresModMagicN = new java.util.Hashtable(500);
	static {
		for (long i = 0; i <= (magicN >> 1); i++)
			squaresModMagicN.put(BigInteger.valueOf((i * i) % magicN), ONE);
	}

	/*	If result is true then n may or may not be a square. If the result is false
		then n cannot be a square. The 'false positive' rate is 3.3% = 336 / 10080 =
		1 / 30. The smallest non-square for which isProbableSquare(n) = True is 385.
	*/
	private final static boolean isProbableSquare(BigInteger n) {
		return squaresModMagicN.get(n.remainder(bigMagicN)) != null;
	}

	/*	Test 'number' many numbers starting at 'start'	*/
	private final static void testisProbableSquare(BigInteger start, long number) {
		while (number-- > 0) {
			if (!isProbableSquare(start) && isSquare(start)) {
				System.out.println("isProbableSquare(" + start + ") failed.\n");
				return;
			}
			start.add(ONE);
		}
		System.out.println("All tests of isProbableSquare successful!");
		System.out.println(isProbableSquare(BigInteger.valueOf(385)) ? "yes\n" : "no\n");
	}

	/*	Compute the jacobi symbol (b / p)
		Precondition: p = 1 mod 4
		Postcondition: Result is jacobi symbol (b / p) or -1 if gcd(b, p) > 1
	*/
	private final static int jacobi(long b, BigInteger p) {
		int s = 1;
		long a = p.mod(BigInteger.valueOf(b)).longValue();
		while (a > 1) {
			if ((a & 3) == 0) a >>= 2;
			else if ((a & 1) == 0) {
				if (((b & 7) == 3) || ((b & 7) == 5)) s = -s;
				a >>= 1;
			}
			else {
				if (((a & 2) == 2) && ((b & 3) == 3)) s = -s;
				long t = b % a; b = a; a = t;
			}
		}
		// Return -1 also in case gcd(a, b) > 1 to ensure termination of /***/ below
		return a == 0 ? -1 : s;
	}

	// All primes less than 500 (499 = ip(95))
	private final static long[] primes = {
		2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59,
		61, 67, 71, 73, 79, 83, 89, 97, 101, 103, 107, 109, 113,
		127, 131, 137, 139, 149, 151, 157, 163, 167, 173, 179, 181,
		191, 193, 197, 199, 211, 223, 227, 229, 233, 239, 241, 251,
		257, 263, 269, 271, 277, 281, 283, 293, 307, 311, 313, 317,
		331, 337, 347, 349, 353, 359, 367, 373, 379, 383, 389, 397,
		401, 409, 419, 421, 431, 433, 439, 443, 449, 457, 461, 463,
		467, 479, 487, 491, 499
	};

	private final static BigInteger lastPrecomputedPrime =
		BigInteger.valueOf(primes[primes.length - 1]);
	private static BigInteger cachePrime = lastPrecomputedPrime;
	private static int cacheN = primes.length; // Invariant: cacheN >= primes.length

	// Result is the n-th prime, nthPrime(n) = 2 for n <= 1
	private final static BigInteger nthPrime(int n) {
		BigInteger result;
		if (n < 1) return TWO;
		if (n <= primes.length) return BigInteger.valueOf(primes[n - 1]);
		if (n < cacheN) {
			result = lastPrecomputedPrime;
			for (int i = primes.length; i < n; i++)
				result = nextProbablePrime(result.add(TWO), primeCertainty);
		}
		else {
			result = cachePrime;
			for (int i = cacheN; i < n; i++)
				result = nextProbablePrime(result.add(TWO), primeCertainty);
			cacheN = n;
			cachePrime = result;
		}
		return result;
	}

	//	Result is the product of the first n primes
	private final static BigInteger primeProduct(int n) {
		BigInteger result = ONE;
		for (int i = 1; i <= n; i++)
			result = result.multiply(nthPrime(i));
		return result;
	}

	/*	Find a probable prime with certain properties
		Precondition: (modulus > 0) and (gcd(modulus, remainder) = 1)
		Postcondition: result p is an odd probable prime and (p >= minimum)
				and (p = remainder mod modulus)
			result = ZERO iff modulus < 1
			result = ONE iff gcd(modulus, remainder) > 1
	*/
	private final static BigInteger primeMod(BigInteger minimum, BigInteger modulus,
			BigInteger remainder) {
		if (modulus.compareTo(ONE) < 0) return ZERO;
		if (!modulus.gcd(remainder).equals(ONE)) return ONE;
		if (minimum.compareTo(TWO) < 0) minimum = TWO;
		BigInteger n = minimum.subtract(remainder).divide(modulus).
			multiply(modulus).add(remainder);
		if (n.compareTo(minimum) < 0) n = n.add(modulus);
		while (!isProbablePrime(n, primeCertainty)) n = n.add(modulus);
		return n;
	}

	/*	Compute the factorial
		Precondition: integer n
		Postcondition: Result is factorial of n and 1 if n < 2
	*/
	private final static BigInteger factorial(int n) {
		BigInteger result = ONE;
		for (int i = 2; i <= n; i++) {
			result = result.multiply(BigInteger.valueOf(i));
		}
		return result;
	}

	private final static boolean isSquare(BigInteger n) {
		return isqrt(n)[1].signum() == 0;
	}

	/*	Compute an imaginary unit modulo p
		Precondition: (p is prime) and (p = 1 mod 4)
		Postcondition: Result is [x, ONE] where x^2 = -1 mod p or
		result is [x, ZERO] where x^2 = p
		Note that this algorithm might succeed even though p is not prime.
		Example (1)
			p = 3277 = 29 * 113 = 5 mod 8, 2^((p-1)/4) = 2^819 = 128 mod p
			128^2 = -1 mod p
		Example (2)
			p = 3281 = 17 * 193, jacobi(3, p) = -1, 3^((p-1)/4) = 2^820 = 81 mod p
			81^2 = -1 mod p
	*/
	private final static BigInteger[] iunit(BigInteger p) {
		BigInteger r = null;
		if (p.testBit(0) && !p.testBit(1) && p.testBit(2)) r = TWO; // p = 5 mod 8
		else {
			int k = 2;
			long q = 3;
			/*	Loop through the primes to find a quadratic non-residue.
				In case p is a square (could happen as p is not necessarily a prime),
				we check for this fact after some unsuccessful attempts of
				finding a prime q with jacobi(q, p) = -1
			*/
/***/		while (jacobi(q, p) == 1) {
				if (k < primes.length) {
					q = primes[k++];
					if ((q == 229) && isProbableSquare(p)) {
					// reached when decomposing d(1, 4*pp(k), 1) for k > 47
						BigInteger[] sr = isqrt(p);
						if (sr[1].signum() == 0)
							return new BigInteger[]{sr[0], ZERO};
					}
				}
				else {
	/*	Unlikely case that we ran out of precomputed primes.
		We can provoke this case by decomposing a prime p of the form 4*k*pp(n)+1
		since p !=5 mod 8 and jacobi(q, p) = 1 for q <= ip(n).
		Example p = 4*11*pp(95)+1
	*/
					if (r == null) r = BigInteger.valueOf(q);
					r = nextProbablePrime(r.add(TWO), 2);
					q = r.longValue();
				}
			}
			if (r == null) r = BigInteger.valueOf(q);
		}
		return new BigInteger[]{r.modPow(p.shiftRight(2), p), ONE};
	}

	/*	Compute the integer square root of n or a number which is too large by one
		Precondition: n >= 0 and 2^log2n <= n < 2^(log2n + 1), i.e. log2n = floor(log2(n))
		Postcondition: Result sr has the property (sr[0]^2 - 1) <= n < (sr[0] + 1)^2 and (sr[0]^2 + sr[1] = n)
	*/
	private final static BigInteger[] isqrtInternal(BigInteger n, int log2n) {
		if (n.compareTo(MAXINT) < 1) {
			int ln = n.intValue(), s = (int)java.lang.Math.sqrt(ln);
			return new BigInteger[]{BigInteger.valueOf(s), BigInteger.valueOf(ln - s * s)};
		}
		if (n.compareTo(ITERBETTER) < 1) {
			int d = 7 * (log2n / 14 - 1), q = 7;
			BigInteger s = BigInteger.valueOf((long)java.lang.Math.sqrt(n.shiftRight(d << 1).intValue()));
			while (d > 0) {
				if (q > d) q = d;
				s = s.shiftLeft(q);
				d -= q;
				q <<= 1;
				s = s.add(n.shiftRight(d << 1).divide(s)).shiftRight(1);
			}
			return new BigInteger[]{s, n.subtract(s.multiply(s))};
		}
		int log2b = log2n >> 2;
		BigInteger mask = ONE.shiftLeft(log2b).subtract(ONE);
		BigInteger[] sr = isqrtInternal(n.shiftRight(log2b << 1), log2n - (log2b << 1));
		BigInteger s = sr[0];
		BigInteger[] qu = sr[1].shiftLeft(log2b).add(n.shiftRight(log2b).and(mask)).divideAndRemainder(s.shiftLeft(1));
		BigInteger q = qu[0];
		return new BigInteger[]{s.shiftLeft(log2b).add(q), qu[1].shiftLeft(log2b).add(n.and(mask)).subtract(q.multiply(q))};
	}

	/*	Compute the integer square root of n
		Precondition: n >= 0
		Postcondition: Result sr has the property sr[0]^2 <= n < (sr[0] + 1)^2 and (sr[0]^2 + sr[1] = n)
	*/
	private final static BigInteger[] isqrt(BigInteger n) {
		if (n.compareTo(MAXINT) < 1) {
			long ln = n.longValue();
			long s = (long)java.lang.Math.sqrt(ln);
			return new BigInteger[]{BigInteger.valueOf(s), BigInteger.valueOf(ln - s * s)};
		}
		BigInteger[] sr = isqrtInternal(n, n.bitLength() - 1);
		if (sr[1].signum() < 0) {
			return new BigInteger[]{sr[0].subtract(ONE), sr[1].add(sr[0].shiftLeft(1)).subtract(ONE)};
		}
		return sr;
	}

	/*	Test harness for the isqrt function
		Perform "number" tests starting with the value of "n"
	*/
	private final static void testisqrt(BigInteger n, long number) {
		BigInteger[] sr;
		BigInteger r, s, s1, s12, s2;
		System.out.println("Start test at " + n.toString() + " for " + new Long(number).toString() + " iterations.\n");
		for (long i = 0; i < number; i++) {
			sr = isqrt(n);
			s = sr[0];
			s2 = s.multiply(s);
			r = sr[1];
			s1 = s.add(ONE);
			s12 = s2.add(s.shiftLeft(1)).add(ONE);
			if ((s2.compareTo(n) > 0) || (s12.compareTo(n) < 1) || (s2.add(r).compareTo(n) != 0)) {
				System.out.println("Fail(" + n + ", " + s + ", " + n.subtract(s.multiply(s)) + ")");
			}
			n = n.add(ONE);
		}
		System.out.println("Test finished.\n");
	}

	/*	Test harness for the iunit function
		Perform "number" tests starting with the value of "start"
	*/
	private final static void testiunit(BigInteger start, long number) {
		BigInteger r, four = BigInteger.valueOf(4);
		start = start.compareTo(four) < 1 ? BigInteger.valueOf(5) :
			start.clearBit(1).setBit(0);
		System.out.println("Start with " + start);
		for (long z = 0; z < number; z++) {
			if (!isProbablePrime(start, primeCertainty)) {
				r = iunit(start)[0];
				if (r.multiply(r).add(ONE).mod(start).signum() == 0)
					System.out.println(start.toString() + " " + r.toString());
			}
			start = start.add(four);
		}
		System.out.println("Stop at " + start);
	}

	// Product of all odd primes less than or equal to 97
	private final static BigInteger primeProduct97 =
		new BigInteger("1152783981972759212376551073665878035");
	private final static BigInteger b341 = BigInteger.valueOf(341L);

	/*	Determine whether n is probably a prime with a given certainty
		Precondition: n odd
		we use the fact that for 2 < i < 341: 2^(i-1) mod i = 1 iff i is prime
		Postcondition: If result is false then n is definitely not a prime
	*/
	private final static boolean isProbablePrime(BigInteger n, int certainty) {
		return ((n.compareTo(b341) < 0) || primeProduct97.gcd(n).equals(ONE)) &&
			TWO.modPow(n.subtract(ONE), n).equals(ONE) &&
			n.isProbablePrime(certainty);
	}

	/*	Compute next probable prime
		Precondition: n odd
		Postcondition: Result is next probable prime p with given certainty and p >= n
	*/
	private final static BigInteger nextProbablePrime(BigInteger n, int certainty) {
		while (!isProbablePrime(n, certainty)) n = n.add(TWO);
		return n;
	}

	/*	Decompose a prime into a sum of two squares
		Precondition: p is (probably) prime and (p = 1 mod 4)
		Postcondition: p = result[0]^2 + result[1]^2 and result[2] = 1
		or result = [0, 0, 0] if p was not a prime
	*/
	private final static BigInteger[] decomposePrime(BigInteger p) {
		BigInteger a = p, b, t, x0 = ZERO, x1 = ONE;
		BigInteger[] sr = iunit(p);
		b = sr[0];
		if (ZERO.equals(sr[1]))
			return new BigInteger[]{ZERO, b, ONE};
		if (b.multiply(b).add(ONE).mod(p).signum() != 0)
			// Failure to compute imaginary unit, p was not a prime
			return new BigInteger[]{ZERO, ZERO, ZERO};
		while (b.multiply(b).compareTo(p) > 0) {
			t = a.remainder(b);
			a = b;
			b = t;
		}
		return new BigInteger[]{a.remainder(b), b, ONE};
	}

	private final static void testdecomposePrime(BigInteger n, long k) {
		BigInteger FOUR = BigInteger.valueOf(4);
		BigInteger[] result;
		n = n.shiftRight(2).shiftLeft(2).add(ONE);
		System.out.println("Start with " + n + " for " + new Long(k) + " iterations.");
		while (k-- > 0) {
			while (!isProbablePrime(n, 2)) {
				n = n.add(FOUR);
			}
			result = decomposePrime(n);
			if (ONE.equals(result[2]) &&
				(result[0].multiply(result[0]).add(result[1].multiply(result[1])).compareTo(n) != 0)) {
				System.out.println("Failure for " + n + ".");
				return;
			}
			n = n.add(FOUR);
		}
		System.out.println("All tests successful. Last prime checked " + n.subtract(FOUR));
	}

	/*	Decompose a positive integer into a sum of at most four squares
		Precondition: n >= 0
		Postcondition: n = result[0]^2 + result[1]^2 + result[2]^2 + result[3]^2
			result[4] = ONE if successful, result[4] = ZERO if failure (no known case)
	*/
	private final static BigInteger[] decompose(BigInteger n) {
		// Check for 0 and 1
		if (n.compareTo(ONE) < 1) return new BigInteger[]{ZERO, ZERO, ZERO, n, ONE};
		BigInteger sq, x, p, delta, v;
		BigInteger[] z, sqp;
		int k = n.getLowestSetBit() >> 1; // n = 4^k*m and (m != 0 mod 4)
		if (k > 0) {
			v = ONE.shiftLeft(k);
			n = n.shiftRight(k << 1);
		}
		else v = ONE;
		// The following two checks are not strictly necessary but the result looks nicer
		// Case 1: Check for perfect square, in this case one square is sufficient
		sqp = isqrt(n);
		sq = sqp[0];
		if (sqp[1].signum() == 0) // n is a perfect square
			return new BigInteger[]{ZERO, ZERO, ZERO, v.multiply(sq), ONE};
		// Case 2: Check for prime = 1 mod 4, in this case two squares are sufficient
		if (n.testBit(0) && !n.testBit(1) && isProbablePrime(n, primeCertainty)) {
			z = decomposePrime(n);
			if (ONE.equals(z[2]))
				return new BigInteger[]{ZERO, ZERO, v.multiply(z[0]), v.multiply(z[1]), ONE};
			delta = ZERO;
		}
		else if (n.testBit(0) && n.testBit(1) && n.testBit(2)) {
			/*	n = 7 mod 8, need four squares
					Subtract largest square sq1^2 such that n > sq1^2 and sq1^2 != 0 mod 8
			*/
			if (sq.testBit(0) || sq.testBit(1)) {
				delta = v.multiply(sq);
				n = sqp[1];
			}
			else {
				delta = v.multiply(sq.subtract(ONE));
				n = sqp[1].add(sq.shiftLeft(1).subtract(ONE));
			}
			sqp = isqrt(n); // Recompute sq, n cannot be a perfect square (n(old) = 7 mod 8)
			sq = sqp[0];
		}
		else delta = ZERO;
		/*	Postcondition: (sq = isqrt(n)) && (n != 7 mod 8) && (n != 0 mod 4)
			This implies that n is a sum of three squares - now check whether n
			is one of the special cases the rest of the algorithm could not handle.
		*/
		int[] special = (int[])specialCases.get(n); // look up in hash table
		if (special != null)
			return new BigInteger[]{delta,
				v.multiply(BigInteger.valueOf(special[0])),
				v.multiply(BigInteger.valueOf(special[1])),
				v.multiply(BigInteger.valueOf(special[2])), ONE};
		/*	Case n = 3 mod 4 (actually n = 3 mod 8)
			Attempt to represent n = x^2 + 2*p with p = 1 mod 4 and p is prime
			Then we can write p = y^2 + z^2 and get n = x^2 + (y+z)^2 + (y-z)^2
		*/
		if (n.testBit(0) && n.testBit(1)) {
			if	(sq.testBit(0)) {
				x = sq;
				p = sqp[1].shiftRight(1);
			}
			else {
				x = sq.subtract(ONE);
				p = sqp[1].add(sq.shiftLeft(1).subtract(ONE)).shiftRight(1);
			}
			while (true) {
				if (isProbablePrime(p, 2)) {
					z = decomposePrime(p);
					if (ONE.equals(z[2])) {
						return new BigInteger[]{delta, v.multiply(x), v.multiply(z[0].add(z[1])),
							v.multiply(z[0].subtract(z[1])).abs(), ONE};
					}
				}
				x = x.subtract(TWO);
				// No case for the following to return is known
				if (x.signum() < 0) return new BigInteger[]{ZERO, ZERO, ZERO, ZERO, ZERO};
				p = p.add(x.add(ONE).shiftLeft(1)); // Proceed to next prime candidate
			}
		}
		/*	Case n = 1 mod 4 or n = 2 mod 4
			Attempt to represent n = x^2 + p with p = 1 mod 4 and p is prime
			Then we can write p = y^2 + z^2 and get n = x^2 + y^2 + z^2
		*/
		if (n.subtract(sq).testBit(0)) {
			x = sq;
			p = sqp[1];
		}
		else {
			x = sq.subtract(ONE);
			p = sqp[1].add(sq.shiftLeft(1).subtract(ONE));
		}
		while (true) {
			if (isProbablePrime(p, 2)) {
				z = decomposePrime(p);
				if (ONE.equals(z[2])) {
					return new BigInteger[]{delta, v.multiply(x), v.multiply(z[0]), v.multiply(z[1]), ONE};
				}
			}
			x = x.subtract(TWO);
			// No case for the following to return is known
			if (x.signum() < 0) return new BigInteger[]{ZERO, ZERO, ZERO, ZERO, ZERO};
			p = p.add(x.add(ONE).shiftLeft(2)); // Proceed to next prime candidate
		}
	}

	// Simple bubble sort for the first four values in result
	private final static BigInteger[] sort(BigInteger[] result) {
		while (true) {
			int i = 0;
			while ((i < 3) && (result[i].compareTo(result[i+1]) < 1)) i++;
			if (i == 3) return result; // Sorted, otherwise exchange offending pair
			BigInteger t = result[i]; result[i] = result[i+1]; result[i+1] = t;
		}
	}

	private final static class IntegerExpression {
	/*	Expression syntax is as follows:
		factor ::= (number | ("sqrt" | "prime" | "d" | "pp" | "ip")
			"(" expression { "," expression } ")") { "!" }
		expTerm ::= factor [ "^" factor ]
		mulTerm ::= expTerm { ("*" | "/" | "%") expTerm }
		expression ::= ["+" | "-"] mulTerm { ("+" | "-") mulTerm }

		Use a standard recursive descent parser to compute value of expression.
	*/

		private StringTokenizer getNextTokenizer;
		/*	"token" is the next token in the input string. Note that in comparisons we use
			constant_string.equals(token) instead of token.equals(constant_string)
			to simplify handling the case where token is null.
		*/
		private String token;
		private String errorMessage;
		private String processedInput;

		// Create IntegerExpression with given input string
		public IntegerExpression(String inputString) {
			errorMessage = null;
			processedInput = "";
			getNextTokenizer = new StringTokenizer(inputString, " \t+-*/%^(,)!", true);
		}

		// Return last error message
		public String getErrorMessage() {
			return errorMessage;
		}

		// Return input processed so far
		public String getProcessedInput() {
			return processedInput;
		}

		// Set the error message if not done so far
		private final void setErrorMessage(String text) {
			if (errorMessage == null) errorMessage = text;
		}

		// Get next token
		private final void getNext() {
			while (true)
				if (getNextTokenizer.hasMoreTokens()) {
					token = getNextTokenizer.nextToken();
					if (!(" ".equals(token) || "\t".equals(token))) { // Non-white space
						if (errorMessage == null) processedInput += token;
						break;
					}
				}
				else {
					token = null; // End of input reached
					break;
				}
	//		System.out.println("Token \"" + (token == null ? "NULL" : token) + "\"");
		}

		//	expression ::= ["+" | "-"] mulTerm { ("+" | "-") mulTerm }
		private final BigInteger expression() {
			BigInteger r;
			if (token == null) {
				setErrorMessage("Empty start of expression.");
				// Sample producer: ""
				return ZERO;
			}
			if ("+".equals(token)) {
				getNext();
				r = mulTerm();
			}
			else if ("-".equals(token)) {
				getNext();
				r  = mulTerm().negate();
			}
			else r = mulTerm();
			while (true)
				if ("+".equals(token)) {
					getNext();
					r = r.add(mulTerm());
				}
				else if ("-".equals(token)) {
					getNext();
					r = r.subtract(mulTerm());
				}
				else return r;
		}

		//	mulTerm ::= expTerm { ("*" | "/" | "%") expTerm }
		private final BigInteger mulTerm() {
			BigInteger r = expTerm();
			while (true)
				if ("*".equals(token)) {
					getNext();
					r = r.multiply(expTerm());
				}
				else if ("/".equals(token)) {
					getNext();
					BigInteger d = expTerm();
					if (d.signum() == 0) {
						setErrorMessage("Division by zero."); // Sample producer: "1/0"
						return ZERO;
					}
					r = r.divide(d);
				}
				else if ("%".equals(token)) {
					getNext();
					BigInteger d = expTerm();
					if (d.signum() <= 0) {
						setErrorMessage("Modulus must be positive."); // Sample producer: "1%0"
						return ZERO;
					}
					r = r.mod(d);
				}
				else return r;
		}

		//	expTerm ::= factor [ "^" factor ]
		private final BigInteger expTerm() {
			BigInteger r = factor();
			if ("^".equals(token)) {
				getNext();
				BigInteger exponent = factor();
				if ((exponent.signum() >= 0) && (exponent.compareTo(MAXINT) <= 0))
					return r.pow(exponent.intValue());
				setErrorMessage("Exponenent must be non-negative and less than 2^31.");
				// Sample producer: "2^(-1)" or 2^(2^31)"
				return ZERO;
			}
			return r;
		}

		private interface BigIntegerFunction {
			public abstract BigInteger function(BigInteger argument);
		}

		private final BigInteger[] getParameters() {
			Vector result = new Vector(10);
			getNext();
			if ("(".equals(token)) {
				do {
					getNext();
					result.addElement(expression());
				} while (",".equals(token));
				if (")".equals(token)) getNext();
				else	setErrorMessage("\")\" expected.");
							// Sample producer: "ip(1"
			}
			else setErrorMessage("\"(\" expected."); // Sample producer: "ip"
			if (errorMessage == null) {
				BigInteger[] r = new BigInteger[result.size()];
				for (int i = 0; i < r.length; i++) r[i] = (BigInteger)(result.elementAt(i));
				return r;
			}
			return new BigInteger[]{};
		}

		private final BigInteger evaluateFunction(BigIntegerFunction f) {
			BigInteger[] parameters = getParameters();
			if (parameters.length == 1) return f.function(parameters[0]);
			setErrorMessage("Only one parameter expected.");
			// Sample producer: "ip(1,1)"
			return ZERO;
		}

		/*	factor ::= (number | ("sqrt" | "prime" | "d" | "pp" | "ip")
					"(" expression { "," expression } ")") { "!" }
		*/
		private final BigInteger factor() {
			BigInteger r = ZERO;
			if (token == null) {
				setErrorMessage("Empty start of factor."); // Sample producer: "2+"
			}
			else if ("(".equals(token)) {
				getNext();
				r = expression();
				if (")".equals(token)) getNext();
				else setErrorMessage("\")\" expected."); // Sample producer: "(2"
			}
			else if ("sqrt".equalsIgnoreCase(token)) // Integer square root
				r = evaluateFunction(new BigIntegerFunction(){
					public BigInteger function(BigInteger argument) {
						if (argument.signum() < 0) {
							setErrorMessage(
								"Cannot take square root of negative number \""
									+ argument.toString() + "\"."); // Sample producer: "sqrt(-1)"
							return ZERO;
						}
						return isqrt(argument)[0];
					}
				});
			else if ("prime".equalsIgnoreCase(token)) // Next prime
				r = evaluateFunction(new BigIntegerFunction(){
					public BigInteger function(BigInteger argument) {
						return (argument.compareTo(TWO) < 1) ?
							TWO : nextProbablePrime(
								argument.testBit(0) ? argument : argument.add(ONE), primeCertainty);
					}
				});
			else if ("ip".equalsIgnoreCase(token)) // i-th prime
				r = evaluateFunction(new BigIntegerFunction(){
					public BigInteger function(BigInteger argument) {
						if ((argument.signum() > 0) && (argument.compareTo(MAXINT) <= 0))
							return nthPrime(argument.intValue());
						else {
							setErrorMessage("Argument of ip must be positive and less than 2^31.");
							// Sample producer: "ip(0)" or "ip(2^31)"
							return ZERO;
						}
					}
				});
			else if ("pp".equalsIgnoreCase(token)) // Prime product
				r = evaluateFunction(new BigIntegerFunction(){
					public BigInteger function(BigInteger argument) {
						if ((argument.signum() > 0) && (argument.compareTo(MAXINT) <= 0))
							return primeProduct(argument.intValue());
						setErrorMessage("Argument of pp must be positive and less than 2^31.");
						// Sample producer: "pp(0)" or "pp(2^31)"
						return ZERO;
					}
				});
			else if ("d".equalsIgnoreCase(token)) { // Dirichlet prime finder
				BigInteger[] parameters = getParameters();
				if (parameters.length == 3) {
					r = primeMod(parameters[0], parameters[1], parameters[2]);
					if (r.signum() == 0)	setErrorMessage("Modulus must be positive.");
																// Sample producer: "d(1,0,1)"
					else if (r.equals(ONE)) setErrorMessage("Modulus must be prime to remainder.");
																	// Sample producer: "d(1,2,2)"
				}
				else	setErrorMessage("Three parameters expected.");
							// Sample producer: "d(1)"
			}
			else // Now expecting a number
				try {
					r = new BigInteger(token);
					getNext();
				}
				catch(NumberFormatException e) {
					setErrorMessage("\"" + token + "\" is not a legal decimal number.");
					// Sample producer: "a"
				}
			while ("!".equals(token)) { // Postfix factorial operator
				if ((r.signum() > 0) && (r.compareTo(MAXINT) <= 0)) {
					getNext();
					r = factorial(r.intValue());
				}
				else {
					setErrorMessage("Argument to factorial must be positive and less than 2^31.");
					// Sample producer: "0!" or (2^31)!"
					return ZERO;
				}
			}
			return r;
		}

		// Evaluates the expression
		public final BigInteger evaluate() {
			BigInteger r = ZERO;
			getNext();
			try {
				r = expression();
			}
			catch(Exception e) {
				setErrorMessage(e.toString());
			}
			if (token != null)	setErrorMessage("Extra \"" + token + "\" detected.");
													// Sample producer: "1 a"
			return r;
		}

	} // class IntegerExpression

	private final class ComputeThread extends Thread {

		private final String expression;
		private final TextArea resultArea;
		private final Label status;

		public ComputeThread(String expression, TextArea resultArea, Label status) {
			this.expression = expression;
			this.resultArea = resultArea;
			this.status = status;
		}

		// decomposition thread
		public void run() {
			long startTime = System.currentTimeMillis();
			status.setText("Decomposing " +
				(expression.length() > 25 ? expression.substring(0, 25) + "..." : expression));
			final IntegerExpression exp = new IntegerExpression(expression);
			final BigInteger n = exp.evaluate();
			if (exp.getErrorMessage() != null) {
				resultArea.setText("Error in expression detected.\n" +
					exp.getProcessedInput() + "???\n" + exp.getErrorMessage() + "\n");
			}
			else if (n.signum() < 0)
				resultArea.setText("Cannot represent negative number " + n);
			else {
				final BigInteger[] result = sort(decompose(n));
				resultArea.setText(n.toString() + " =\n" +
					(result[0].signum() == 0 ? "" : "\t" + result[0].toString() + "^2 +\n") +
					(result[1].signum() == 0 ? "" : "\t" + result[1].toString() + "^2 +\n") +
					(result[2].signum() == 0 ? "" : "\t" + result[2].toString() + "^2 +\n") +
					"\t" + result[3].toString() + "^2" + (n.equals(result[0].multiply(result[0]).
					add(result[1].multiply(result[1])).add(result[2].multiply(result[2])).
					add(result[3].multiply(result[3]))) ? "" : " Fail(" + n + ")"));
			}
			status.setText("Time used = " + Long.toString(System.currentTimeMillis() - startTime) + " ms.");
		}

	} // class ComputeThread

	// return a string consisting of "number" space characters
	private final String spaces(int number) {
		StringBuffer temp = new StringBuffer(number);
		for (int i = 0; i < number; i++) temp.append(' ');
		return new String(temp);
	}

	private final static int textWidth = 85;
	private final TextField numberText = new TextField(textWidth);
	private final Label statusText = new Label(spaces(textWidth));
	private final TextArea resultText = new TextArea(5, textWidth);
	private final Button decomposeButton = new Button("Decompose");
	private final Button clearButton = new Button("Clear");
	private ComputeThread compute = null;

	private final void stopPreviousThread() { // make sure no compute thread is running
		statusText.setText("");
		if (compute != null) {
			try {
				// if (true) throw new SecurityException(); // simulate exception for testing
				compute.stop(); // deprecated but just works fine in this situation
			}
			catch (SecurityException e) {
				statusText.setText("Waiting for decomposition to finish...");
				try {
					compute.join();
				}
				catch (InterruptedException ie) {}
			}
			compute = null;
		}
	}

	private final class ComputeAction implements ActionListener {
		public final void actionPerformed(java.awt.event.ActionEvent e) {
			stopPreviousThread();
			compute = new ComputeThread(numberText.getText(), resultText, statusText);
			compute.start();
		}
	} // class ComputeAction

	private final static Color backgroundColor = new Color(0xF7F7F7);

	public void init() {
		setFont(new Font("serif", Font.PLAIN, 12));
		setLayout(new FlowLayout(FlowLayout.LEFT));
		setBackground(backgroundColor);
		/*	Make sure not to use Color.WHITE as this was introduced
			only in 1.4.x and this applet should be 1.1 compatible
			Unfortunately the API documentation does not mention this fact
			(see http://developer.java.sun.com/developer/bugParade/bugs/4466493.html )
		*/
		resultText.setBackground(Color.white);
		numberText.setBackground(Color.white);
		add(new Label("Enter an integer expression to be decomposed into a sum of at most four squares " + VERSION));
		add(numberText);
		add(decomposeButton);
		add(clearButton);
		add(resultText);
		add(statusText);
		clearButton.addActionListener(new ActionListener() {
			public final void actionPerformed(java.awt.event.ActionEvent e) {
				stopPreviousThread();
				resultText.setText("");
				numberText.setText("");
				numberText.setCaretPosition(0);
				numberText.requestFocus();
			}
		});
		ComputeAction ca = new ComputeAction();
		decomposeButton.addActionListener(ca);
		resultText.setEditable(false);
		numberText.addActionListener(ca);
		numberText.setEditable(true);
		numberText.setText("19081961");
		numberText.selectAll();
		numberText.setCaretPosition(0);
		numberText.requestFocus();
	}

	// Simple frame which terminates the application on a close window event
	private final static class AppletFrame extends Frame {

		public AppletFrame(String name) {
			super(name);
			addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					System.exit(0);
				}
			});
		}

	} // class AppletFrame

	/*	Test harness for the decompose function
			Perform "number" tests starting with the value of "start". Print intermediate
			results iff "verbose" = true.
	*/
	private final static void testDecompose(BigInteger start, long number, boolean verbose) {
		final long startTime = System.currentTimeMillis(), perDot = 250, perLine = 10000;
		long errors = 0;
		final String range = " range [" + start + ", " +
			start.add(BigInteger.valueOf(number - 1)) + "] of size " + Long.toString(number);
		System.out.println("Start" + range + ".");
		BigInteger toBeTested = start;
		for (long i = 1; i <= number; i++) {
			if (i % perDot == 0) System.out.print(".");
			if (i % perLine == 0)
				System.out.println(" (i=" + Long.toString(i) + ", n=" + toBeTested + ")");
			BigInteger[] result = decompose(toBeTested);
			if (verbose) {
				sort(result);
				System.out.println(toBeTested.toString() + " " + result[0] + " " + result[1] +
					" " + result[2] + " " + result[3]);
			}
			if (result[4].longValue() == 0) {
				System.out.println("FAIL(" + toBeTested + ")");
				errors++;
			}
			else if (!result[0].multiply(result[0]).add(result[1].multiply(result[1])).
					add(result[2].multiply(result[2])).add(result[3].multiply(result[3])).
					equals(toBeTested)) {
				System.out.println("Error(" + toBeTested + ")");
				errors++;
			}
			toBeTested = toBeTested.add(ONE);
		}
		System.out.println((number % perLine == 0 ? "" : "\n") +
			"Finished" + range + ". Time used " +
			Long.toString(System.currentTimeMillis() - startTime) + " ms. " +
				(errors == 0 ? "No error detected." :
					(errors == 1 ? "One error detected." : Long.toString(errors) +
						" errors detected.")));
	}

	private final static void startApplet(FourSquares applet, int width, int height) {
		final AppletFrame f = new AppletFrame("Lagrange");
		f.add("Center", applet);
		f.pack();
		f.setSize(width, height);
		applet.setSize(width, height);
		applet.init();
		applet.start();
		f.show();
		f.repaint();
	}

	public final static void main(String[] args) {
//		testisqrt(new IntegerExpression(args[0]).evaluate(), Long.parseLong(args[1])); if (false)
//		testiunit(new IntegerExpression(args[0]).evaluate(), Long.parseLong(args[1])); if (false)
//		testdecomposePrime(new IntegerExpression(args[0]).evaluate(), Long.parseLong(args[1])); if (false)
//		testisProbableSquare(new IntegerExpression(args[0]).evaluate(), Long.parseLong(args[1])); if (false)
		if ((0 < args.length) && (args.length < 4)) {
			try {
				IntegerExpression exp = new IntegerExpression(args[0]);
				BigInteger n = exp.evaluate();
				if (exp.getErrorMessage() == null)
					testDecompose(n, (args.length > 1 ? Long.parseLong(args[1]) : 1),
						(args.length == 1) || (args.length > 2) && (args[2].equalsIgnoreCase("verbose")));
				else
					System.out.println(exp.getErrorMessage() + "\n" + exp.getProcessedInput() + "???");
			}
			catch(Exception e) {
				System.out.println("Exception(" + e +
					")\nUsage: [startExpression [#tests] [\"verbose\"]]");
			}
		}
		else startApplet(new FourSquares(), 750, 270);
	}

}

