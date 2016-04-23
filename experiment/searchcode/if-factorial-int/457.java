package ua.kpi.burdun.diploma.tailcalloptimization;

/**
 * Created by burdun on 02.06.2015.
 */
public class FactorialSimple {

    public static long factorialRec(final long number) {
        if (number == 1)
            return number;
        else
            return number * factorialRec(number - 1);
    }

    public static int factorialRec_TailCall(final int factorial, final int number) {
        if (number == 1)
            return factorial;
        else
            return factorialRec_TailCall(factorial * number, number - 1);
    }


    public static void main(String[] args) {
//        System.out.println(factorialRec(10000));
        System.out.println(factorialRec_TailCall(1, 14000));
    }
}

