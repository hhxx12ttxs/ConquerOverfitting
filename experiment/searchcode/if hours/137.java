package Refatoreerimine;

public class Calc {
	
    public static final int JUNIOR = 1;
    public static final int SENIOR = 2;
    public static final int SPECIALIST = 3;
    
    int wage;

    public static void main(final String[] args) {
        
    }
    protected int juniorWage(final int type,final int workingHours) {
    	wage = 0;
        if (workingHours > 8) { // if longer than eight hours
        	wage = 10 * (workingHours - 8) * 2; // double pay
            wage += 10 * 8;
            if (workingHours>20){
                wage+=10;
                }
        }else {
            wage += 10 * workingHours;
        }
        return wage;
    }
    protected int seniorWage(final int type, final int workingHours){
    	wage = 0;
        if (workingHours > 8) { // if longer than eight hours
            wage = 15 * (workingHours - 8) * 2; // double pay
            wage += 15 * 8;
            if (workingHours>20) {
                wage += 20;
            }	
        }else {
            wage += 15 * workingHours;
        }
        return wage;
    }
    protected int specialistWage(final int type, final int workingHours){
    	wage = 0;
        if (workingHours > 9) { // if longer than nine hours
        	wage = 22 * (workingHours - 9) * 3; // triple pay after 9 hours
            wage += 22 * 9;
            if (workingHours>20){
                wage +=30;
            }
        } else {
            wage += 22 * workingHours;
        }
        return wage;
    }
}

