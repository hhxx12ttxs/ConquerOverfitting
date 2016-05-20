package de.eva.prime;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to calculate prime numbers using the sequential prime number test.
 * @author Christian M채hlig, Dan H채berlein
 *
 */
public class Prime {

	protected int lowerBound;
	protected int upperBound;
	
	protected List<Integer> resultList;

	public Prime(int lowerBound, int upperBound) {
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
		resultList = new ArrayList<Integer>();
	}

	public void createPrimeList() {
		/* simple prime number test, having the complexity of O(n) 
		 * good example for multithreading in terms of complexity
		 */
		if (lowerBound % 2 == 0)
			++lowerBound;
		for (int i = lowerBound; i <= upperBound; i += 2) {
			boolean isPrime = true;
			for (int j = 3; j < i; ++j) {
				if (i % j == 0) {
					isPrime = false;
					break;
				}
			}
			if (isPrime == true && i >= 2)
				resultList.add(i);
		}
	}
	
	public List<Integer> getResultList() {
		return resultList;
	}
}

