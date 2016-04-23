package sid.rane.recursion;


public class Factorial {
    public Factorial() {
        super();
    }
    
    public int factorial(int n){
        if(0==n)
            return 1;
        else 
            return n*factorial(n-1);
    }
    
    public static void main(String[] args) {
        Factorial factorial = new Factorial();
        int fact = factorial.factorial(3);
        System.out.println("Fact="+fact);
    }
}

