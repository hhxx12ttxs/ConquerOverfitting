package org.codesandtags.ejercicios;

import java.math.BigInteger;

public class FactorialNumero {

	public static void main(String[] args) {

		FactorialNumero resultado = new FactorialNumero();

		for (int i = 1; i < 25; i++) {
			System.out.println("Factorial de " + i + " : "
					+ resultado.obtenerFactorial(i));
		}

	}

	public long obtenerFactorial(int numero) {
		if(numero > 20 ){
			System.err.println("Error : No se puede obtener factorial de [" + numero + "]");
			return 0;
		}
		
		long factorial = 1;

		for (int i = numero; i >= 1; i--) {
			factorial *= i;
		}

		return factorial;
	}
	
	public String obtenerFactorialBigInteger(int numero){
		BigInteger factorial = BigInteger.ONE;
		
		return factorial.toString();
	}
}

