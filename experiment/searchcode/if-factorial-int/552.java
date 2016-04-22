public class Factorial{
	
	static int recursiveFactorial(int iInput)
	{
		 if (iInput == 1)
		        return 1;
		    else
		        return iInput * recursiveFactorial(--iInput);
		
	}
	
	public static void main(String[] args){
		int iInput = 5;
		int iFactorial = 1;
		
		while(iInput>1){
			iFactorial = iFactorial * iInput--;		
		}
		
		System.out.println(recursiveFactorial(9));
		
	}
	
}
