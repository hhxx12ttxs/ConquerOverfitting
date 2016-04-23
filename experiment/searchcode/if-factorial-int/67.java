package objetos;

import ejerciciosMetodos.LeerTeclado;

public class FactorialRecursivo {

	
	public static void main(String[] args) {
		int n;
		
		do {
			System.out.println("Introduce un nş positivo o cero");
			n=LeerTeclado.readInteger();
		}
		while (n<0);
		System.out.println("El factorial de "+n+" es "+factorial(n));
	

	}
	
	public static double factorial (int x){
		//double result;
		if (x==0)
			return 1;
			
			//result=1;
		else  return x*factorial(x-1);
		
		//return result;
		
		
			
	}

}

