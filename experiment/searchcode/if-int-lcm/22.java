package danieladeyemo.projecteuler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Daniel
 * Date: 30/10/13
 * Time: 19:00
 */
public class Library {

	private Library() {
	}

	public static int largestPrimeFactor(int n) {
		int p = smallestFactor(n);

		while (p < n) {
			n /= p;
			p = smallestFactor(n);// (p<n) == true iff n is non-prime
		}
		return n;
	}

	public static long largestPrimeFactor(long n) {
		long p = smallestFactor(n);

		while (p < n) {
			n /= p;// n /= p == largest factor, so we are going thru all the largest factors in decreasing order until p==n i.e. largest factor is prime
			p = smallestFactor(n);
		}
		return n;
	}

	// computes the prime factorization of a positive integer and prints it.
	public static void primeFactorize(int n) {
		int i;
		for (i = 2; i <= Math.sqrt(n); i++) {
			if (n % i == 0) {
				int cnt = 0;
				while (n % i == 0) {
					n /= i;
					cnt++;
				}
				System.out.print(i + "^" + cnt + ", ");
			}
		}
		if (n % i != 1)
			System.out.print(n + "^" + 1);
	}

	// returns list containing prime numbers up to N(inclusive)
	// employs the Sieve of Eratosthenes
	public static List<Integer> allPrimesUpTo(int N) {
		boolean isComposite[] = new boolean[N + 1]; // all initialized to false
		List<Integer> list = new ArrayList<Integer>();
		for (int i = 2; i <= (Math.sqrt(N)); i++) { // the factors of any number can be found found form 2...<=sqrt(n)
			if (!isComposite[i])
				list.add(i);
			for (int s = i * i; s <= N; s += i)
				isComposite[s] = true;
		}
		for (int i = (int) Math.sqrt(N) + 1; i <= N; i++) {
			if (!isComposite[i])
				list.add(i);
		}
		return list;
	}

	// returns a list containing all factors of an integer .
	public static List<Integer> allFactors(int n) {
		List<Integer> factors = new ArrayList<Integer>();
		factors.add(1);
		int sqrt = (int) Math.sqrt(n);
		int i = 2;
		while (i <= sqrt) {
			if (n % i == 0)
				factors.add(i);
			i++;
		}
		i = factors.get(factors.size() - 1);
		while (i > 1) {
			if (n % i == 0 && i != sqrt)
				factors.add(n / i);
			i--;
		}
		factors.add(n);
		return factors;
	}

	// smallest factor of n == smallest prime factor
	public static int smallestFactor(int n) {
		for (int i = 2, end = (int) Math.sqrt(n); i <= end; i++) {
			if (n % i == 0)
				return i;
		}
		return n;
	}

	public static long smallestFactor(long n) {
		for (long i = 2, end = (long) Math.sqrt(n); i <= end; i++) {
			if (n % i == 0)
				return i;
		}
		return n;
	}

	// boolean isPrime(){}
	static boolean isPrime(int N) {
		if (N < 2)
			return false;
		for (int i = 2; i * i <= N; i++)
			if (N % i == 0)
				return false;
		return true;
	}

	static boolean isPrime(long N) {
		if (N < 2)
			return false;
		for (int i = 2; i * i <= N; i++)
			if (N % i == 0)
				return false;
		return true;
	}

	static boolean isPrime(String N) {
		long n = Integer.parseInt(N);
		if (n < 2)
			return false;
		for (int i = 2; i * i <= n; i++)
			if (n % i == 0)
				return false;
		return true;
	}


	// boolean isPalin(){}
	public static boolean isPalin(String s) {
		int a = 0;
		int b = s.length() - 1;
		while (s.charAt(a) == s.charAt(b) && a < b) {
			a++;
			b--;
		}
		if (a < b)
			return false;
		else
			return true;
	}

	public static boolean isPalin(int i) {
		return isPalin(i + "");
	}


	// Euclid's GCD(){}
	public static long GCD(long a, long b) {
		if (b == 0)
			return a;
		return GCD(b, a % b);
	}


	// Least Common Multiple
	public static long LCM(long a, long b) {
		return a * (b / GCD(a, b));
	}

	public static long LCM(int... a) {
		long result = a[0];
		for (int i = 1; i < a.length; i++) {
			result = Library.LCM(result, a[i]);
		}
		return result;
	}
}
