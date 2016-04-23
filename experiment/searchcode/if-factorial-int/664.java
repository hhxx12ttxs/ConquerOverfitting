package metodos;

import utiles.Teclado;

/**
* Define la clase Factorial que muestre el factorial de un número introducido
* por teclado. (Factorial (4) = 4*3*2*1; Factorial (0) = 1) Realízalo con los
* tres bucles (factorialFor(), factorialDoWhile(), factorialWhile()).
*
* @author Rafa Miranda
* @version 1.0
*/

public class Factorial{
	public static void main (String[]args){
		int n;

		System.out.println("\nIntroduce un numero y calculare su factorial.");
		n = Teclado.leerEntero();

		System.out.println("\nEl factorial de su numero con For es: "+factorialFor(n));
		System.out.println("\nEl factorial de su numero con Do...While es: "+factorialDoWhile(n));
		System.out.println("\nEl factorial de su numero con While es: "+factorialWhile(n));
	}

	/**
	* Metodo para calcular el factorial de un numero con bucle For.
	*
	* @param n es el numero del que queremos calcular su factorial
	* @return Resultado total del factorial.
	*/
	static int factorialFor(int n){
		int total=1;
		if (n==0)
			return total=1;
		else{
			for(int i=1; n>=i; i++){ 
			total *= i;
			} 
			return total;
		}
	}

	/**
	* Metodo para calcular el factorial de un numero con bucle Do...While.
	*
	* @param n es el numero del que queremos calcular su factorial
	* @return Resultado total del factorial.
	*/
	static int factorialDoWhile(int n){ 
		int i=1, total=1;
		if (n==0)
			return total=1;
		else{
			do{
				total *= i;
				i++;
			}while (n>=i);
		}
		return total;
	}

	/**
	* Metodo para calcular el factorial de un numero con bucle While.
	*
	* @param n es el numero del que queremos calcular su factorial
	* @return Resultado total del factorial.
	*/
	static int factorialWhile(int n){
		int i=1, total=1;
		if (n==0)
			return total=1;
		else{
			while (n>=i){
			total *= i;
			i++;
			}
		}
	return total;
	}
}
