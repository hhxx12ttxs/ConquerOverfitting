public class Factorial {
	
	public int factorial(int numero){
		int fact = 0;
		for (int i = 0; i <= numero; i++) {
			fact = calcFactorial(i);
		}
		return fact;
	}
	
	int calcFactorial(int numeroActual) {
		if (numeroActual <= 1)
			return 1;
		else
			return numeroActual * calcFactorial(numeroActual - 1);
	}
}

