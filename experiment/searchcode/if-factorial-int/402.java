import utiles.*;
/**
*Define la clase Factorial que muestre el factorial de un número introducido por teclado.
*(Factorial (4) = 4*3*2*1; Factorial (0) = 1) Realízalo con los tres bucles (factorialFor(), factorialDoWhile(), factorialWhile())
*
*@author Roberto Carlos Flores Gomez
*@version 1.0
*/
public class Factorial{
	public static void main(String[] args) {
		int numero;
		do{
			numero=Teclado.leerEntero("Dime un numero: ");
			factorialFor(numero);
			factorialDoWhile(numero);
			factorialWhile(numero);
		}while(deseaContinuar());
	}

	/*
	* Realiza el factorial con el bucle Do While
	*/
	static void factorialDoWhile(int numero){
		int factorial=1, i=numero;
		System.out.println("Factorial con Do While.");
		if(numero==0){
			System.out.println("El factorial de "+numero+" es 1");
			return;
		}
		do{
			factorial*=i;
			i--;
		}while(i>0);
		System.out.println("El factorial de "+numero+" es "+factorial);
	}

	/*
	* Realiza el factorial con el bucle While
	*/
	static void factorialWhile(int numero){
		int factorial=1, i=numero;
		System.out.println("Factorial con While.");
		while(i>0){
			factorial*=i;
			i--;
		}
		System.out.println("El factorial de "+numero+" es "+factorial);
	}

	/*
	* Realiza el factorial con el bucle For
	*/
	static void factorialFor(int numero){
		int factorial=1;
		System.out.println("Factorial con For.");
		for (int i=numero; i>0; i--) {
			factorial*=i;
		}
		System.out.println("El factorial de "+numero+" es "+factorial);
	}

	/**
	* Pregunta al usuario si desea continuar
	*@return la respuesta del usuario
	*/
	static boolean deseaContinuar(){
		char respuesta;
		respuesta=Teclado.leerCaracter("Desea continuar? (s/n)");
		if(respuesta=='s' || respuesta=='S')
			return true;
		else
			return false;
	}
}
