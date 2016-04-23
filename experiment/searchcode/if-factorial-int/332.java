import java.util.Scanner;

public class factorial_recursion

{

	public static void main(String[] svjb)
	{
	//input number to take factorial of 
	int factorial_input=number_entry_for_factorial_calculation();	

	//Call function to calculate factorial of number
    long factorial_result=factorial_calculator(factorial_input)	;
	//output message showing factorial of an integer we chose
	System.out.println(factorial_input+" ! is "+ factorial_result);
	}

	static int number_entry_for_factorial_calculation()
	{
		Scanner integer_for_factorial=new Scanner(System.in);
		System.out.println("Please input the number you want to take the factorial of");
		int number_to_factorize=integer_for_factorial.nextInt();

		return number_to_factorize;
	}

	static long factorial_calculator(int factorial_input_number)
	{
		if (factorial_input_number ==0 || factorial_input_number==1)
		{
			return 1;
		}

		else
		{
		    return factorial_input_number*factorial_calculator(factorial_input_number-1);
		}
	
	}


}

