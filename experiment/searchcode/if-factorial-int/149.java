package uk.co.sammy.classes;

public class Factorial {
	public int factorial(int number){
		if(number == 0){
			return 1;
		}
		return number * factorial(number - 1);
	}
}

