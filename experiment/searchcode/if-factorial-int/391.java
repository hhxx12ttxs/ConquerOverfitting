package by.epam.jmp.multithreading;

import by.epam.jmp.multithreading.bean.FactorialResult;

/**
 * Created by Alexandr on 07.02.2016.
 */
public class FactorialService{

    public static FactorialResult calculateFactorial(int number){
        return new FactorialResult(number, factorial(number));
    }

    public static int factorial(int n)
    {
        if (n == 0){
            return 1;
        }
        return n * factorial(n-1);
    }
}

