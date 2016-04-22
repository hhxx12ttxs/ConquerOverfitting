package gamess;

import java.util.Scanner;

public class Factorial
{
    public static int factorial(int z)
    {
        if(z==0)
        {
            return 1;
        }
        return z*factorial(z-1);
    }
}

