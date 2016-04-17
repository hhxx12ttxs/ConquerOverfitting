package garage;

/**
 *A simple utility class for FeeCalcStrategies to use, because many methods would be repeated in each
 * @author Zachary
 */
public class FeeCalcUtil {
    /**
     * This method simply calls both checkMin and roundUp for the input values
     * @param hours - the hours to be checked for min and rounded up
     * @param hoursBase - the minimum hours allowable, or 0 if there is no minimum
     * @return the highest between inputs hours, hoursBase, and 0, rounded up to the nearest int
     */
    public static double checkAndRound(double hours, double hoursBase){
        double temp = FeeCalcUtil.checkMin(hours,hoursBase);
        double out = FeeCalcUtil.roundUp(temp);
        return out;
    }
    /**
     * This method is used to ensure that a double is at least another double
     * this is validation for FeeCalcs, and should always be used. If there is
     * no minimum hours to park, 0 should be used to hoursBase
     * @param hours - this is the hours to be at least a minimum
     * @param hoursBase - this is the minimum of allowable hours, or 0 if there is no minimum
     * @return this will return the hours param, unless it is negative
     * or less than hoursBase, then it will return the greater of hoursBase and 0
     */
    public static double checkMin(double hours, double hoursBase){
        if(hoursBase < 0){
            hoursBase = 0 ;
        }//minimum time is a distance, so it can't be negative
        double checked = hours;
        if(hours < hoursBase){
            checked = hoursBase;
        }//this is error handling, it makes sure I don't multiply by negative
        return checked;
    }
    /**
     * This method will round the input double up to the nearest int.
     * This is useful to calculate all partial hours in a garage as whole hours
     * @param toRound - the double to be rounded up
     * @return a double that is rounded to the closest greater int
     */
    public static double roundUp(double toRound){
        double rounded = toRound;
        if(toRound > ((int)toRound * 1.0)){
            rounded = (int)toRound + 1;
        }//this ensures that any part of an hour counts as an hour.
        return rounded;
    }
}

