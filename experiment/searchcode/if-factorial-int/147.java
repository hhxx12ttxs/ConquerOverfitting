public class Factorial{
	public static int obtenerFactorial(int numero){
		int factorial=1;
		if (numero<0){
			System.out.println("No hay factorial de números negativos");
			System.exit(1);
		}
		else {
			for (int i=1;i<=numero;++i)
				factorial*=i;		
		}
		return factorial;
	}
}

