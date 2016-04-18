import java.io.*;
import java.text.DecimalFormat;
public class Tuition
{
 public static void main(String[] args) throws IOException
 {
  int hours;
  double fees, rate, tuition;
  displayWelcome();
  hours = getHours();
  rate = getRate(hours);
  tuition = calcTuition(hours, rate);
  fees = calcFees(tuition);
  displayTotal(tuition + fees, hours);
 }
 public static void displayWelcome()
 {
  System.out.println("Welcome to the Student Financial Tuition Calculator Thingy");
 }
 public static int getHours() throws IOException
 {
  String strHours;
  int hours = 0;
  BufferedReader dataIn = new BufferedReader(new InputStreamReader(System.in));
  System.out.print("Please enter the amount of hours: ");
  strHours = dataIn.readLine();
  try
  {
   hours = Integer.parseInt(strHours);
   if (hours < 0) throw new NumberFormatException();
  }
  catch(NumberFormatException e)
  {
   System.out.println("Enter positive whole numeric values only please.");
   return getHours();
  }
  return hours;
 }
 public static double getRate(int hours)
 {
  double rate = 0.00;
  if(hours < 15)
  {
   rate = 44.50;
  }
  else
  {
   rate = 50.00;
  }
  return rate;
 }
 public static double calcTuition(int hours, double rate)
 {
  double tuition = hours * rate;
  return tuition;
 }
 public static double calcFees(double tuition)
 {
  double fees = tuition * 0.08;
  return fees;
 }
 public static void displayTotal(double total, int hours)
 {
  DecimalFormat twoDigits = new DecimalFormat("$#,000.00");
  if (hours > 15)
  {
   System.out.println("You are a Full Time Student!");
  }
  else if (hours == 0)
  {
   System.out.println("You are currently not enrolled to any credit hours!");
  }
  else
  {
   System.out.println("You are a Part Time Student!");
  }
  System.out.println("Total Amount Due: " + twoDigits.format(total));
 }
}
