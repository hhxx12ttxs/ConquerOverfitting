package test;

public class factorial {
	public static void main(String [] args){
		int input =Integer.parseInt("5");
		double result = factor(input);
		System.out.println(result);
	}
	
	public static double factor (int x){
		assert x==2 : "Error";
		if (x<=0)
			return 0.0;
		double fact = 1.0;
		while (x>1){
			fact=fact*x;
			x=x-1;
		}
		return fact;
	}
}

