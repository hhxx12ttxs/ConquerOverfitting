public class factorial {
    
    public static void main(String [] args) {
        long answer = factorial(60);
        System.out.println(answer);
    }
    
    public static long factorial(int x) {
        if (x <= 1) {
            return 1;
        } else {
            return x * factorial(x - 1);
        }
    }
}
