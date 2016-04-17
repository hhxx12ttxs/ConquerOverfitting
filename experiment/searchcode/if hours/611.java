/**
 * Convert to Army Time
 * HW 4, part 2
 * CSC 211 602
 * @JustinWhite
 */

import java.util.Scanner;

public class convertTime {
  
  public static int convertHour(int hours_12, String am_pm){
    int hours_24;
    if(hours_12 != 12 && am_pm.equals("pm") || am_pm.equals("PM")){
      hours_24 = hours_12 + 12;
    } else if( hours_12 == 12 && am_pm.equals("am") || am_pm.equals("AM")){
      hours_24 = 0;
    } else 
      hours_24 = hours_12;
    return hours_24;
  }
  
  public static void printTime(int hours_24, int minutes){
    if(hours_24 >= 0 && hours_24 < 10 && minutes >9)
      System.out.println("0"+hours_24 + ":" + minutes);
    else if(hours_24 >= 0 && hours_24 < 10 && minutes >= 0 && minutes < 10)
      System.out.println("0"+hours_24 + ":" + "0"+minutes);
    else if(hours_24 >= 10 && minutes < 10)
      System.out.println(hours_24 + ":" + "0"+minutes);
    else
      System.out.println(hours_24 + ":" + minutes);
  }
  
  
  public static void main(String [ ] args) {
    String response = "y";
    while (response.equals("y") || response.equals("Y")){
      Scanner input = new Scanner(System.in);
      System.out.println("Enter a time to be converted. for example 4 30 pm");
      int hours_12 = input.nextInt();
      int minutes = input.nextInt();
      String am_pm = input.next();
    
      int hours_24 = convertTime.convertHour(hours_12, am_pm);
      convertTime.printTime(hours_24, minutes);
      System.out.println("Continue? y/n");
      response = input.next();
    }
    System.out.println("Goodbye.");
  }
  
}
  

  
