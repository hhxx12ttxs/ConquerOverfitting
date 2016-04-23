
public class Algorithm_Factorial {

	int result;

	Algorithm_Factorial(int n) {
		result = calcurateFactorial(n);
	}

	int calcurateFactorial(int n) {
		if(n>1)
			return (n) * calcurateFactorial(n-1);
		else
			return n;
	}
	
	int returnResult()
	{
		return result;
	}

}

