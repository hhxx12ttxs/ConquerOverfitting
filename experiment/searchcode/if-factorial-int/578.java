package recursive;

public class Factorial {
	
	public int factorial(int num){
		int n=num;
		if(num == 1){
			return num;
		}
		else{
			num--;
			factorial(num);
			return n*factorial(num);
		}
	}
	
	public static void main(String[] args){
		Factorial factor = new Factorial();
		int product = factor.factorial(5);
		System.out.println(product+"=="+(5*24));
	}

}

