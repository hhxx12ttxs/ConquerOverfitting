package ee.tty.praktikum2;

public class Calc {

    public static final int JUNIOR = 1;
    public static final int SENIOR = 2;
    public static final int SPECIALIST = 3;
    
    public static void main(final String[] args) {
 
    }

    protected static int payJunior(final int type,final int hoursWorked) {
        int Sum = 0;
        if (type == JUNIOR) {
            if (hoursWorked > 8) {
                Sum = 10 * (hoursWorked - 8) * 2;
                Sum += 10 * 8;
                if (hoursWorked > 20) {
                	Sum += 10;
                }
            } else {
                Sum += 10 * hoursWorked;
            }
        }
        return Sum;
    } 
    
    protected static int paySenior(final int type,final int hoursWorked) {
    	int Sum = 0;
    	if (type == SENIOR) {
            if (hoursWorked > 8) {
                Sum = 15 * (hoursWorked - 8) * 2;
                Sum += 15 * 8;
                if (hoursWorked > 20) {
                	Sum += 20;
                }           
            } else {
                Sum += 15 * hoursWorked;
            }
        }
    	return Sum;
    }
    	
    protected static int paySpecialist(final int type,final int hoursWorked) {
    	int Sum = 0;
    	if (type == SPECIALIST) {
            if (hoursWorked > 9) {
                Sum = 22 * (hoursWorked - 9) * 3;
                Sum += 22 * 9;
                if (hoursWorked > 20) {
                	Sum += 30;
                }
            } else {
                Sum += 22 * hoursWorked;
            }
        }
        return Sum;
    }
}
