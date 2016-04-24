package Ejercicio3;

import Ejercicio2.LeerTeclado;

public class Factorial {
	
		private int num;
	
		public Factorial(){
			System.out.println("Introduce numero");
			num=LeerTeclado.readInteger();
			
			if(num<=0)throw new ArithmeticException();
			else{
					verFactorial(num);
			}
		}
		
		public int verFactorial(int num){
			if (num==0)
				return num=1;
			 else
			 {
				 System.out.println("Fact: "+num);
			    return num * verFactorial(num-1);
			 }
		}
}
