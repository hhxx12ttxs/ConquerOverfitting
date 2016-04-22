package training.ideas.java.datatypes;

/**
 * Created by idnmao on 8/20/2014.
 */
public class Factorial {
    public static int calcFactorialOf(int myinputnum) {

        int calculatedFactorial=0;
        calcFact(calculatedFactorial,myinputnum);
        return calculatedFactorial;
    }
    
    public static void calcFact(int calculatedFactorial, int f){
        if (f==1){
            return;
        }
        calcFact(calculatedFactorial,f-1);
        calculatedFactorial=calculatedFactorial*f;
    }

}

