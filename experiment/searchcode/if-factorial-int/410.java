package spoj;

public class FCTRL2 {

    public static void main(String[] args) {
        System.out.println(factorial(5));
        System.out.println(factorial(6));
        System.out.println(factorial(1  ));
    }

    private static int factorial(int n) {
        if (n <= 2) {
            return n;
        }
        return n * factorial(n-1);
    }
}

