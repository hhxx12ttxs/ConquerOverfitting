import java.util.Scanner;

class Factorial{
	public static int factorial(int entero){
		int factorial;

		if(entero < 0){
			factorial=-1;
		} else {
			if(entero==0){
				factorial=1;
			} else {
				factorial=entero;
				for(int i=entero-1; i > 0; i--){
					factorial*=i;
				}
			}
		}

		return factorial;
	}
	
	public static void main(String[] args) {
		int entero;
		Scanner sc = new Scanner(System.in);
		System.out.print("\tCalculo de factorial\n"
				+ "Numero entero: ");
		entero=sc.nextInt();

		System.out.println("\nFactorial: " + factorial(entero));
	}
}
