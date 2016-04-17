import java.text.DecimalFormat;

/**
 * Created by kevindlee on 7/19/15.
 */
public class FooCorporation {
    public static void main(String[] args) {
        double employeeBasepay[] = { 7.50, 8.20, 10.0 };
        int employeeHours[] = {35, 47, 73 };

        for( int i=0; i < 3; i++){
            calculatePay(employeeBasepay[i],employeeHours[i]);
        }

    }

    /**
     *
     * @param basepay
     * @param hours
     * @return the pay earned
     */
    public static final double calculatePay(double basepay, int hours){
        DecimalFormat df = new DecimalFormat("0.00");
        // Overtime rate is 1.5x more when more than 40 hours
        if (hours > 40.0) {
            basepay = 1.5 * basepay;
        }
        // Must be making at least minimum wage $8/hour
        if ( basepay < 8.0 ){
            System.out.println("Error: base pay is less than minumum wage of $8: $" + df.format(basepay));
            return 0.0;
        }
        // Cannot work more than 60 hours per week
        if ( hours > 60 ) {
            System.out.println("Error: too many hours, must be <= 60: " + hours);
            return 0.0;
        }
        double pay = basepay * hours;
        System.out.println("base pay: $" + df.format(basepay) + " hours: " + hours + " Total Pay: $" + df.format(pay));
        return basepay * hours;
    }
}

