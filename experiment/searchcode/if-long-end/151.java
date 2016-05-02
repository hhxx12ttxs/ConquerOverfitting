package util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Scanner;

/*
 * ???????????
 */
public class IsPrimeOld {

//	private static List<Long> primeList = new ArrayList<Long>();
	private static PrimesListContainer primeList = new PrimesListContainer();

	private static long maxNumber = 3;

	private static long newMaxNumber = 0;

	private boolean isPrimer;

	private long number;

	public IsPrimeOld(long number) throws UnexpectedNumberException {
		this.number = number;
		if (number == 1L || number <= 0) {
			throw new IsPrimeOld.UnexpectedNumberException();
		} else if (number == 2L || number == 3L) {
			this.isPrimer = true;
		} else if (number < maxNumber) {
			this.isPrimer = initIsPrimer();
		} else {
			newMaxNumber = number;
			initPrimeList();
			this.isPrimer = initIsPrimer();
		}
	}

	/**
	 * @param args
	 * @throws UnexpectedNumberException
	 */
	public static void main(String[] args) throws UnexpectedNumberException {

		long number = 110;
		Scanner scanner = new Scanner(System.in);
		while (true) {
			log("input the number please(" + 0 + "~" + Long.MAX_VALUE + "):");
			number = scanner.nextLong();
			log(number + " is prime?" + IsPrimer(number));
		}

	}

	private static void initPrimeList() {

		log("create prime list...");
		long start = System.currentTimeMillis();
		if (maxNumber == 3) {
			primeList.add(2L);
			primeList.add(3L);
		}
		for (long i = maxNumber; i < newMaxNumber; i += 2) {
			long j = 1;
			double temp = Math.sqrt(i);
			for (j = 2; j < temp; j++) {
				if (i % j == 0)
					break;
			}
			if (j == (long) temp + 1) {
				// log(i);
				primeList.add(i);
			}
		}
		long end = System.currentTimeMillis();
		log((end - start) + " milliseconds");
		log("size:" + primeList.size());
		maxNumber = newMaxNumber;
	}

	public static boolean IsPrimer(long number)
			throws UnexpectedNumberException {
		IsPrimeOld isPrime = new IsPrimeOld(number);
		return isPrime.isPrimer;
	}

	private boolean initIsPrimer() {
		log("calc is primer...");
		long start = System.currentTimeMillis();
		boolean b = true;
		for (Long primer : primeList) {
			if (number % primer == 0) {
				b = false;
				break;
			}
		}
		long end = System.currentTimeMillis();
		log((end - start) + " milliseconds");
		return b;
	}

	@Override
	public String toString() {
		return isPrimer ? "true" : "false";
	}

	public boolean isPrimer() {
		return isPrimer;
	}

	private static void log(Object obj) {
		System.out.println(obj);
	}

	public static class UnexpectedNumberException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public UnexpectedNumberException() {
			super("Can NOT be 1 or any number under 0.");
		}

	}

	public static class PrimesListContainer implements Iterable<Long> {
		private final static int INIT_LENGTH = 1000000;

		private final static int RENEW_LENGTH = 1000000;

		private Long[] container = new Long[INIT_LENGTH];

		private int size = INIT_LENGTH;

		private int length = 0;

		private int current = 0;

		public void add(Long number) {
			container[length] = number;
			length++;
			if (length == size) {
				reNewContainer();
			}

		}
		
		public int size()
		{
			return length;
		}

		private void reNewContainer() {
			size += RENEW_LENGTH;
			container = Arrays.copyOf(container, size);
		}

		public Iterator<Long> iterator() {
			return new Iterator<Long>() {

				public boolean hasNext() {
					return current < length;
				}

				public Long next() {
					return container[current++];
				}

				public void remove() {
					// do nothing
				}
			};
		}

	}

}

