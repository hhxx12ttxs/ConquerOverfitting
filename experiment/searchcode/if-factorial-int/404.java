
public class Factorial {
	
	public int factorial(int element) {
		if(element == 0){
			return 0;
		} else if(element == 1) {
			return 1;
		} else {
			return element * factorial(element -1);
		}
	}
	
	public static void main(String[] args) {
		
		System.out.println(new Factorial().factorial(10));
	}

}

