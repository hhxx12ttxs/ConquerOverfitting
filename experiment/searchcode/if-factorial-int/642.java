package ch4;

/**
 * Created by ikirilov on 06/02/15.
 */
public class FactorialTestNG {

    public long CalculateFactorial(int b){
        if(b==1){
            return (b);
        }
        else {
            return (b * CalculateFactorial(b - 1));
        }
    }
}

