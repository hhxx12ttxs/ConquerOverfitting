
public class Ex14 {
	static int Factorial(int number){
		if(number==0){
			return 1;
		}
		return number*Factorial(number-1);
	}
	public static void main(String[] args) {
		System.out.println(Factorial(5));

	}

}

