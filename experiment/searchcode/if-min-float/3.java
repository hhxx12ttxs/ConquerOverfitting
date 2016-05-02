package id5530405617.junsa.jugarin.lab3;

public class SimpleStats {

	public static void main(String[] args) {
	
		float  sum = 0,agv = 0,min ,max = 0;
	
		
		System.out.println("For the input GPAs: ");
		for(int i = 1 ; i < args.length  ; i++){
        	System.out.print(args[i] + " ");
            sum += Float.parseFloat(args[i]);
        	}
		
		agv = sum/(args.length - 1);
		
		for(int i = 1 ; i < args.length ; i++){
			
			if(Float.parseFloat(args[i]) > max){
			max = Float.parseFloat(args[i]);
			}}
		
		min = Float.parseFloat(args[0]);
		for(int i = 1 ; i < args.length -1 ; i++){
			
			if(min > Float.parseFloat(args[i]) ){
			min = Float.parseFloat(args[i]);
			}}
		
		
		System.out.println("\nStatus:");
		System.out.println("Avg GPA is " + agv);
		System.out.println("Min GPA is " + min);
		System.out.println("Max GPA is " + max);
	
    }
}	

