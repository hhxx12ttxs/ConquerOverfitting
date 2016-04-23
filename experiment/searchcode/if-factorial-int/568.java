import java.util.Scanner;
public class factorial
{
		private static Scanner scanner=new Scanner(System.in); static{scanner.useDelimiter(System.getProperty("line.separator"));}
	public static void main(String[] args)
	{
Factorial first_factorial=new Factorial();

System.out.println("what number would you like to know the factorial for ");
first_factorial.n=scanner.nextInt();
System.out.println("what number would you like to know the factorial for "+first_factorial.factorial_m(first_factorial.n));

}}

class Factorial{private static Scanner scanner=new Scanner(System.in); static{scanner.useDelimiter(System.getProperty("line.separator"));}
	int n;
	int result=0;

	int factorial_m(int n){
		if(n==0){
			return n;
		}
		else{
			this.result=this.result*factorial_m(n-1);
		}
		return n;
	}
}

