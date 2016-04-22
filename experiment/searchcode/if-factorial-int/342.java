import utiles.*;
/**
* Define  la  clase  Factorial  que  muestre el  factorial  de  un  número  introducido  por teclado.
* (Factorial  (4)  =  4*3*2*1;  Factorial  (0)  =  1) 
* Realízalo  con los  tres  bucles (factorialFor(), factorialDoWhile(), factorialWhile())
* @author Antonio Luque Bravo
* @version 1.0
*/
public class Factorial{
	public static void main(String[] args) {
		int factorial = Teclado.leerEntero("Dame un numero para calcularle su factorial..");
		factorialFor(factorial);
		factorialWhile(factorial);
		factorialDoWhile(factorial);
	}
	/**
	* M&eacute;todo para calcular el factorial con bucle for.
	* @param num n&uacute;mero para calcular su factorial.
	* @return Devuelve el resultado del factorial.
	*/
	static void factorialFor(int num){
		if(num==0)
			System.out.println("Factorial (" + num + ") = 1");
		else{
			System.out.print("Factorial ("+ num +") = ");
			System.out.print(num);
			for(int i=num-1;i>0;i--){
				System.out.print("*"+i);
				num*=i;
			}
			System.out.print(" = " + num + "\n");
		}
	}
	/**
	* M&eacute;todo para calcular el factorial con bucle while.
	* @param num n&uacute;mero para calcular su factorial.
	* @return Devuelve el resultado del factorial.
	*/
	static void factorialWhile(int num){
		int i=num-1;
		if(num==0)
			System.out.println("Factorial (" + num + ") = 1");
		else{
			System.out.print("Factorial ("+ num +") = ");
			System.out.print(num);
			while(i>0){
				System.out.print("*" + i);
				num*=i;
				i--;
			}
			System.out.print(" = " + num + "\n");
		}
	}
	/**
	* M&eacute;todo para calcular el factorial con bucle do-while.
	* @param num n&uacute;mero para calcular su factorial.
	* @return Devuelve el resultado del factorial.
	*/
	static void factorialDoWhile(int num){
		int i=num-1;
		if(num==0)
			System.out.println("Factorial (" + num + ") = 1");
		else{
			System.out.print("Factorial ("+ num +") = ");
			System.out.print(num);
			do{
				System.out.print("*"+i);
				num*=i;
				i--;
			}while(i>0);
			System.out.print(" = " + num + "\n");
		}

	}
}

