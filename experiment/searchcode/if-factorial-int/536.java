package curso.ejemplos.basicos;

public class Factorial {

	public static int factorialNumero(int numero){
		int res = 0;
		
		if (numero == 1)
			res = numero;
		else
			res = numero * factorialNumero(numero-1);

		return res;
	}
}

