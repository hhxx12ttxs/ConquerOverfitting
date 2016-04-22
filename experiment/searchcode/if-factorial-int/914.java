package recursive;

public class Factorial {

	//Calculo de Factorial recursivo
	
	public static void main(String[] args) {
		for (int i = 0; i < 10; i++){
			System.out.println("O factorial de " +i+ " � "+ Factorial.calFact(i));
		}
	}
	
	private static int calFact(int n){
		if (n <= 1)
			return 1;
		else 
			return n * calFact(n -1);
	}
}

