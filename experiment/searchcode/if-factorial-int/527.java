package datatype.numbers;

import org.junit.Assert;
import org.junit.Test;

public class FactorialRecursive {

	@Test
	public void testFactorial() {
		
		int number = 5;
		int computeFactorial = factorial(number);
		Assert.assertEquals(120, computeFactorial);
		
	}
	
	private int factorial(int number){
		if(number == 1){
			return 1;
		}
		return number * factorial(--number);
	}

}

