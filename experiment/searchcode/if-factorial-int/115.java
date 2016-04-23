public class Factorial {
    public int factorial(int n) {
        if (n == 1) {
            return n;
        } else {
            return n * factorial(n-1);
        }
    }
    
    public static void main(String[] args) {
        Factorial num = new Factorial();
        
        System.out.println(num.factorial(5));
    }
}

