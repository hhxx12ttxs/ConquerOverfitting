package javalab.day3;

public class Day3_Factorial {

	
	
	public static void main(String[] args){

		System.out.println("Factorial(6) = " + factorial(6));
		
		
	}
	
	
	static Integer factorial(int f){
		
		
		if (f == 1){
		  return f;
		}else{
		
		  
		  int fac =  f * factorial(f -1);
		  
		  return fac;
					
		   
		}
				
		
	}
	
}

