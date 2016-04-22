
public class factorialIterative {
public static void main(String[] args) {
	System.out.println(factorial(0));
}

static int factorial(int input)
{
	if(input < 0)
	{
		System.out.println("error");
		return 0;
	
	}
	
	if(input == 0)
		return 1;
	
	int x, factorial =1;
	for(x=input;x>1;x--)
		factorial*=x;
	
	return factorial;
}
}

