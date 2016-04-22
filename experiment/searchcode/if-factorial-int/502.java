
public class Factorial {

	public static int f_Factorial(int a){
		
		if (a == 1)
			return 1;
		else
			return a * f_Factorial(a-1);
	}
		
	public static void main(String[] args) {	
		int n = 10;
		int result = f_Factorial(n);
		System.out.printf("El factorial de %d es %d.\n",n, result);
		
		int r = 5;
		result = f_Factorial(r);
		System.out.printf("El factorial de %d es %d.\n",r, result);
		
		/*int r = 5;
		result = f_Factorial(r);
		System.out.printf("El factorial de %d es %d.\n",r, result);
		
		int r = 5;
		result = f_Factorial(r);
		System.out.printf("El factorial de %d es %d.\n",r, result);*/
		
		int f = 8;
		result = f_Factorial(f);
		System.out.printf("El factorial de %d es %d.\n",f, result);
	}
}

