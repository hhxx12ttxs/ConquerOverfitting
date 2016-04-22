/**
 * Created by daftnemesis on 16/08/15.
 */
public class Factorial {

    public static void main(String[] args) {

        for (int i = 1; i <= 20; i++){
            System.out.println("El factorial de " + i + " es " + factorial(i));
        }

    }

    public Factorial(){

    }

    public static long factorial(long n){
        if (n == 1)
            return 1;
        else
            return n * factorial(n -1);
    }
}

