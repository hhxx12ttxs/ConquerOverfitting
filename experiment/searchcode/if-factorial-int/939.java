package recursion.basic_recursion;

public class RecursiveFactorialExample {
	public static void main(String[] args) {
		int i = 16; //Anything over 16! causes some integer overflow problems
		System.out.println(i + "! = " + factorial(i));
	}
	
	public static int factorial(int i){
		if (i == 1){
			return 1;
		}else{
			return i * factorial(i-1);
		}
	}
}

