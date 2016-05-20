/**
 * If we list all the natural numbers below 10 that are multiples of 3 or 5, we 
 * get 3, 5, 6 and 9. The sum of these multiples is 23. 
 * Find the sum of all the multiples of 3 or 5 below 1000.
 */
package problem1;

/**
 * @author ahalim
 * 
 */
public class SumOfNaturalNumbers {
	public SumOfNaturalNumbers() {

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SumOfNaturalNumbers sum = new SumOfNaturalNumbers();
		System.out.println("The sum: "+sum.sumOfAllElementsOfAnArray(sum
				.findMultiplesOfNAndM(3, 5, 0, 100)));
		sum.printAllElementsOfTheArray(sum.findMultiplesOfNAndM(3, 5, 0, 100));

	}

	public int[] findMultiplesOfNAndM(int n, int m, int start, int end) {
		int[] multiplesOfNAndM = new int[end - start];
		int index;
		int i = 0;
		for (index = start; index < end; index++) {
			if ((index % n == 0 || index % m == 0) && index != 0) {
				multiplesOfNAndM[i] = index;
				i++;
			}
		}
		return multiplesOfNAndM;
	}

	public int sumOfAllElementsOfAnArray(int[] theArray) {
		int sum = 0;
		for (int i = 0; i < theArray.length; i++) {
			sum += theArray[i];
		}
		return sum;
	}

	public void printAllElementsOfTheArray(int[] theArray) {
		for (int i = 0; i < theArray.length; i++) {
			if (i != 0 && i % 10 == 0) {
				if(theArray[i] == 0) {
					i = theArray.length;
					break;
				}
				System.out.print(theArray[i] + "\t");
			}
		}
	}
}

