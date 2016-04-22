
public class Factorial {

    public static void main(String[] args) {
        // TODO code application logic here
        int number = 5;
        System.out.println("factorial of "+number +"  is : "+factorial(number));
    }

    private static int factorial(int i) {
        
        if(i <= 1)
            return 1;
        else
            return i * factorial(i-1);
    }
    
}
