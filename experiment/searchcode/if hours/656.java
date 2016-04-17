/*
    Program 213a Weird Wages
    Wesley Rogers 
    11/18/15
    Java 1.8u25, using Eclipse Mars
    Windows 7

    Gives the amount of money you make given the hours worked in file Prog213a.dat

    What I learned: Not much.
    
    Difficulties: Getting sunday and saturday's values was a bit akward but that's it.
*/

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class WeirdWages {
    public static final double pay = 30;
    public static final double extraHoursD = 25.5; //For days with 8+ hours
    public static final double extraHoursW = 15; //For weeks with 40+ hours
    public static final double satBonusP = 1.25; 
    public static final double sunBonusP = 1.5;
    
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        double totalMade = 0;
        int totalHours = 0;
        int[] hours = new int[7]; 
        Scanner input = null;
        int week = 1;
        try {
            input = new Scanner(new File("Prog213a.dat"));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        while(input.hasNextLine()){ 
            
            for(int i = 0; i<7; i++){ //This resets itself! :D
                hours[i] = input.nextInt();
            }
       
           totalHours = hours[0] + hours[1]+hours[2]+hours[3]+ hours[4]+hours[5]+hours[6];
           if (totalHours >= 40){
               totalMade += totalHours*(extraHoursW);
           }
       
           for(int dayH : hours){
               if (dayH >= 8){
                   totalMade += (dayH-8)*(extraHoursD);
               }
           }
           
           totalMade += totalHours*pay;
       
           { //Saturday and Sunday Block
               if (hours[0] > 8){ // Remove sunday's pay so we can add a bonus to it
                   totalMade -= pay*8 + (hours[0]-8)*extraHoursD;
               } else {
                   totalMade -= pay*8;
               }
           
               if (hours[6] > 8){ // Remove sunday's pay so we can add a bonus to it
                   totalMade -= pay*8 + (hours[6]-8)*extraHoursD;
               } else {
                   totalMade -= pay*8;
               }
           
               if (hours[0] > 8){ //Add back sunday's pay with the bonus
                   totalMade += sunBonusP*(pay*8 + (hours[6]-8)*extraHoursD);
               } else {
                   totalMade +=sunBonusP*pay*8;
               }
           
               if (hours[6] > 8){ //Add back saturday's pay with the bonus
                   totalMade +=satBonusP*(pay*8 + (hours[6]-8)*extraHoursD);
               } else {
                   totalMade +=satBonusP*pay*8;
               }
           }
           
           System.out.print("Hours worked: ");
           for (int day :hours){
               System.out.print(day + " ");
           }
           
           
           
           System.out.println("\nTotal amount made during week " + week +": $" + totalMade + "\n"); 
           totalMade = 0;
           totalHours = 0;
           week++;
       }
    }
}
/*
Hours worked: 9 8 10 8 9 9 5 
Total amount made during week 1: $2777.25

Hours worked: 7 8 8 8 0 8 9 
Total amount made during week 2: $2371.875

Hours worked: 6 10 5 0 0 0 0 
Total amount made during week 3: $861.0

Hours worked: 24 24 24 24 24 24 24 
Total amount made during week 4: $10902.0

Hours worked: 1 2 3 4 5 6 14 
Total amount made during week 5: $1421.25
*/
