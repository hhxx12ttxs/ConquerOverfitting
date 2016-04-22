
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Factorial {
    public static void main(String args[]) throws NumberFormatException, IOException{
        int factorialNumber = 0;
        int factorialResult = 1;
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(isr);
        System.out.print("Please Enter the number:");
        factorialNumber = Integer.parseInt(br.readLine());
        
        /*for(int i=1;i<=factorialNumber;i++)
        {
            factorialResult = factorialResult*i;
        }*/
        /**********************Factorial with Recursion***********************/
        factorialResult = factorial(factorialNumber);
        /**********************Factorial with Recursion***********************/
        System.out.println("Factorial of Number "+factorialNumber+" is: "+factorialResult);
    }
    /**********************Factorial with Recursion***********************/
    static int factorial(int fn) {
        if(fn<=1)
        {
            return 1;
        }
        else
        {
            return fn*factorial(fn-1);
        }
    }
    /**********************Factorial with Recursion***********************/
}

