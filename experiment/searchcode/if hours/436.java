import java.io.*;
import java.util.*;

public class Solution {

  public static void main(String[]args){

    Scanner sc = new Scanner(System.in);

    String timeIn = sc.nextLine();
    String[] temp = timeIn.split(":");
    String hours = temp[0];
    String mins = temp[1];
    String secs = temp[2].substring(0,2); // Substring to get the seconds out.
    String AMPM = temp[2].substring(2,4);

    if ( AMPM.equalsIgnoreCase("PM") ) {
      int intHours = Integer.parseInt(hours);
      if(intHours < 12) {
        hours = Integer.toString(intHours += 12);
      }
    } else {
      int intHours = Integer.parseInt(hours);
      if(intHours == 12) {
        hours = "00";
      }
    }
    System.out.println(hours + ":" + mins + ":" + secs);
  }

}

