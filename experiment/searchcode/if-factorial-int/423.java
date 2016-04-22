package others;

public class Factorial {

	public static void main(String args [])
	{
		System.out.println(factorial(5));
	}
	public static int factorial(int number)
	{
		
		if(number==0)
			return 1;
		else
		if(number==1)
		return 1;
		else
		return factorial(number-1)+factorial(number-2);
	}

}

