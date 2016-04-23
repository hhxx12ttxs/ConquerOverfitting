class Factorial{
	public static void main(String[] args){
		int number = 6;
		
		System.out.println(factorial(number));
	}
	
	public static int factorial(int number){
		if(number == 2){
			return number;
		}
		return number * factorial(number - 1);
	}
}
