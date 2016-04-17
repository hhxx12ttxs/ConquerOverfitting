package ee.tty.praktikum2;

//represents reusable domain service which calculates daily salaries
public class Calc {
    //employee types
    public static final int JUNIOR = 1;
    public static final int SENIOR = 2;
    public static final int SPECIALIST = 3;   
    public static final int JUNIOR_PAY = 10;
    public static final int SENIOR_PAY = 15; 
    public static final int SPECIALIST_PAY = 22;
    public static final int REGULAR_WORK_HOURS = 8;
    public static final int SPECIALIST_WORK_HOURS = 9;   

    //my crappy screen test

    public static int pay(final int type, final int hours, final int pay, final int workHours){
        int sum = 0;
	           if (hours > workHours) { // if longer than eight hours
	               sum = pay * (hours - workHours) * 2 + pay * workHours; // double pay
	               if (hours > 20) { 
	            	   if (type == JUNIOR){
	            		   sum +=pay;
	            	   }else if(type == SENIOR){
	            		   sum+=20;
	            	   }
	               }
	            } else {
	                sum += pay * hours;
	            }
	         return sum;
    }
	
	public static int specialistPay(final int type, final int hours){
		int sum = 0;
	            if (hours > SPECIALIST_WORK_HOURS) { // if longer than nine hours
	                sum = SPECIALIST_PAY * (hours - SPECIALIST_WORK_HOURS) * 3 + SPECIALIST_PAY * SPECIALIST_WORK_HOURS ; // triple pay after 9 hours
	                if (hours > 20) {
	                	sum +=30;
	                }
	        } else {
	            sum += SPECIALIST_PAY * hours;
	        }
	        return sum;
        }
	}

