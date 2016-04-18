/*
 * Assignment 2: Wage Calculator
 */

class FooCorporation{
  public static void pay(double base_pay, int hours){
    if (base_pay < 8.0){
      System.out.println("Error: Base pay cannot be under minimum wage.");
    } else if (hours > 60){
      System.out.println("Error: Number of hours worked greater than 60.");
    } else {
      int overtime = 0;
      if (hours > 40){
        overtime = hours - 40;
        hours = 40;
      }
      System.out.println("Employee Wages: $" + ((hours * base_pay) + (overtime * base_pay * 1.5)));
    }
  }

  public static void main(String[] args){
    pay(7.50, 35);
    pay(8.20, 47);
    pay(10.0, 73);
  }
}


