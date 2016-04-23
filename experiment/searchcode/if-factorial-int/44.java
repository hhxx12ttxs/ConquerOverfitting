

public class FactorialRecursivo {

	public static void main(String[] args) {
		int n;
		do {
			System.out.println("Introduce nş positivo");
			n = LeerTeclado.readInteger();
		} while (n < 0);
		
		System.out.println("El factorial de n es: " + factorial(n));

	}

	public static double factorial(int i) {
		double aux;
		if (i == 0)
			aux = 1;
		else aux=i*factorial(i-1);
		return aux;
		
	}
}

