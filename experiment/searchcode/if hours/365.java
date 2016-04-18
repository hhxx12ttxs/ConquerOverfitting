package ylesanne;

public class dailySalariesCalc {

  public static final int JUNIOR_EMPLOYEE = 1;
  public static final int SENIOR_EMPLOYEE = 2;
  public static final int SPECIALIST_EMPLOYEE = 3;
  public static final int NORMAL_WORKING_HOURS = 8;
  public static final int SPECIALIST_NORMAL_WORKING_HOURS = 9;


  public static void main(final String[] args) {
      dailySalariesCalc c = new dailySalariesCalc();

  }
  public static int juniorPay(final int workingHours){
	  
	  int Salary = 0;
	  if (workingHours > NORMAL_WORKING_HOURS) { 
          Salary = 10 * (workingHours - NORMAL_WORKING_HOURS) * 2;
          Salary += 10 * NORMAL_WORKING_HOURS;
	  if(workingHours > 20){
    	  Salary += 10;
      }
	  }else {
          Salary += 10 * workingHours;
      }
	  return Salary;
  }
  
  public static int seniorPay(final int workingHours){

	  int Salary = 0;
	  if (workingHours > NORMAL_WORKING_HOURS) { 
          Salary = 15 * (workingHours - NORMAL_WORKING_HOURS) * 2;
          Salary += 15 * NORMAL_WORKING_HOURS;
          if(workingHours > 20){
    	  Salary += 20;
      }
          }else {
          Salary += 15 * workingHours;
      }
	  return Salary;
  }
  
  public static int specialistPay(final int workingHours){

	  int Salary = 0;
	  if (workingHours > SPECIALIST_NORMAL_WORKING_HOURS) { 
          Salary = 22 * (workingHours - SPECIALIST_NORMAL_WORKING_HOURS) * 3;
          Salary += 22 * SPECIALIST_NORMAL_WORKING_HOURS;
      if(workingHours > 20){
    	  Salary += 30;
      }
      }else {
          Salary += 22 * workingHours;
      }
	  return Salary;
  }
  

}
