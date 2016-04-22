import TerminalIO.*;

public class Factorial {
  
    public static void main(String[] args)
    {
        KeyboardReader k=new KeyboardReader();
      int f=k.readInt("Enter the factorial (max 15): ");
        
        System.out.println(factorial(f));
    }
    
    static int factorial (int n)
        {
            if (n==1)
                return 1;
            else
                return n * factorial (n-1);
        }

}

