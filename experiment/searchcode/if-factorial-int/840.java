public class Factorial {
    public static void main(String[] args) {
        
        System.out.println(recursiveFactorial(7));
        System.out.println(iterativeFactorial(7));
        
    }
    
    public static int recursiveFactorial(int n) {
        if (n==1) {
            return 1;
        }
        else {
            int result = n * recursiveFactorial(n-1);
            
            return result;
        }
    }
    
    public static int iterativeFactorial(int n) {
        int count = n;
        int result = 1;
        
        while (count > 1) {
            result *= count;
            count--;
        }
        return result;
    }
}
        
