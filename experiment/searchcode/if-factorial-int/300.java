
public class Factorial {
	public static void main(String[] args){
		Factorial f = new Factorial();
		int number = f.factorial(5);
		System.out.println(number);
	}
	public int factorial(int number){
		if(number == 0){
			return 1;
		}
		System.out.println(number);
		return number * factorial(--number);
	
	}
}

