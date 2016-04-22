/**
 * @author khru
 */
public class Factorial 
{
	public static void main(String[] args) 
	{
		Scanner teclado = new Scanner(System.in);
		int num, factorial;
		System.out.println("Introduce un número positivo a calcular su factorial: ");
		num = teclado.nextInt();
		if(num <= 0)
		{
			error();
		}
		else
		{
			//factorial = factorial(num);
			System.out.println(factorial(num));
		}
	}
	public static int factorial(int num)
	{
		int factorial =1 ;
		for(int i = num;i >= 1;i--)
		{
			factorial *= i;
		}
		return factorial;
	}
	public static void error()
	{
		System.out.println("El parametro introduccido es erroneo");
	}
}

