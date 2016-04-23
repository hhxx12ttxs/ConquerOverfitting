public class Recursive1 {
	
	public static void main(String[] args)
	{
		System.out.println("5! = " + factorial(5));
	}

	static int factorial(int n){
		if (n == 1)
		{	//�禡�פ���!!
			return 1;
		}
		else
		{
			return n * factorial(n - 1);
		}
	}
}
