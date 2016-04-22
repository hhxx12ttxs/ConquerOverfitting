package factorialrecursivo;

public class FactorialRecursivo {

    static int factorial(int n) {
        
        if (n > 0) {
            return n * factorial(n - 1);
        } else {
            return 1;
        }

    }

    public static void main(String[] args) {

        System.out.println("El factorial de 5 es: " + factorial(5));

    }

}

