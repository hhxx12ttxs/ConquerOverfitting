package kodune;

//represents reusable domain service which calculates daily salaries
public class Calc {
	
  public static final int JUNIOR = 1;
  public static final int SENIOR = 2;
  public static final int SPECIALIST = 3;
  
  public static final int juniorPay = 10;
  public static final int juniorExtraHoursPay = 10;
  public static final int seniorPay = 15;
  public static final int seniorExtraHoursPay = 20;
  public static final int specialistPay = 22;
  public static final int specialistExtraHoursPay = 30;
  
  public static final int doublePayHoursStart = 8;
  public static final int triplePayHoursStart = 9;
  public static final int extraBonusHoursStart = 20;

  public static void main(final String[] args) {
      Calc c = new Calc();
     System.out.println(c.pay(1, 5));
     System.out.println(c.pay(1, 10));
     System.out.println(c.pay(1, 24));
     System.out.println(c.pay(2, 5));
     System.out.println(c.pay(2, 10));
     System.out.println(c.pay(2, 24));
     System.out.println(c.pay(3, 5));
     System.out.println(c.pay(3, 10));
     System.out.println(c.pay(3, 24));
  }

  protected int pay(final int type,final int hours) {
      int sum = 0;
      if (type == JUNIOR) {
          sum = payJunior(hours);
      }
      if (type == SENIOR) {
         sum = paySenior(hours); 
      }
      if (type == SPECIALIST) {
          sum = paySpecialist(hours);
      }
      return sum;
  }
  
  protected int payJunior(final int hours){
	  int sum = 0;
	  if (hours > doublePayHoursStart) {
          sum += payForDoubleHours(juniorPay, hours);
      } else {
          sum += payForRegularHours(juniorPay, hours);
      } if (hours > extraBonusHoursStart) { //extra bonus for working over 20 hours
          sum += payForExtraHours(JUNIOR);
      }
      return sum;
  }

  protected int paySenior(final int hours){
	  int sum = 0; 
	  if (hours > doublePayHoursStart) { // if longer than eight hours
          sum += payForDoubleHours(seniorPay, hours);
      } else {
          sum += payForRegularHours(seniorPay, hours);
      } if (hours > extraBonusHoursStart) { //extra bonus for working over 20 hours
          sum += payForExtraHours(SENIOR);
      }
	  return sum;
  }
  
  protected int paySpecialist(final int hours){
	  int sum = 0; 
	  if (hours > triplePayHoursStart) { // if longer than nine hours
          sum += payForTripleHours(specialistPay, hours);
      } else {
          sum += payForRegularHours(specialistPay, hours);
      } if (hours > extraBonusHoursStart) { //extra bonus for working over 20 hours
          sum += payForExtraHours(SPECIALIST);
      }
	  return sum;
  }
  
  protected int payForDoubleHours(final int wage, final int hours){
	  int sum = wage * (hours - doublePayHoursStart) * 2;
	  sum += payForRegularHours(wage, 8);
	  return sum;
  }
  
  protected int payForTripleHours(final int wage, final int hours){
	  int sum = wage * (hours - triplePayHoursStart) * 3;
	  sum += payForRegularHours(wage, hours);
	  return sum;
  }
  
  protected int payForRegularHours(final int wage, final int hours){
	  int sum = wage * hours; 
	  return sum;
  }

  protected int payForExtraHours(final int type){
	  if (type == JUNIOR){
		  return juniorExtraHoursPay;
	  } else if (type == SENIOR){
		  return seniorExtraHoursPay;
	  } else if (type == SPECIALIST){
		  return specialistExtraHoursPay;
	  } else{
		  return 0;
	  }
  }
}

