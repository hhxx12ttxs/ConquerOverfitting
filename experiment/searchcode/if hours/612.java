//Matthew Chin
//Date: 9/23/2014
//Course Information: CSE002-110
//Homework 04 Time Padding Program

/*
This program  has the user enter a positive integer giving the number of seconds that 
have passed during the day and then displays the time in conventional form. 
*/

//import Scanner class
import java.util.Scanner;

//TimePadding Class
public class TimePadding{
    
    //main method
    public static void main(String args[]){
        
        //initialize scanner constructor
        Scanner myScanner = new Scanner(System.in);
        
        //variables
        //seconds to be entered
        int timeSec=0;
        int hours=0;    //hours
        int minutes=0;  //minutes
        int seconds=0;  //seconds
        //get inpu from user as int for secs
        System.out.print("Enter the time passed in seconds: ");
        if(myScanner.hasNextInt()){ //check input is an int
            
            timeSec= myScanner.nextInt(); // assign timeSec to input
            
        }//end scanner if
        
        else { //input was not a valid integer at all e.g. ("hi")
            
            System.out.println("You did not enter an integer.");
            return;
        }
        //test timeSec is > 0
        if(timeSec > 0){
        
            //calculate individual components of time
            //included to demonstrate calculation in case of error
            hours = (timeSec/3600)%60;  //hours
            System.out.println("Hours: " + hours);
        
            minutes = (timeSec/60)%60;  //minutes
            System.out.println("Minutes: " + minutes);
        
            seconds = (timeSec%60);     //seconds
            System.out.println("Seconds: " + seconds);
        
        }//end timeSec check if
        
        else if(timeSec < 0 || (hours < 0 && minutes < 0 && seconds < 0)){ //check value is positive int
            
            //output
            System.out.println("You did not enter a positive int.");
            return;
            
        }//end fail if
      
        if((hours >= 0 && hours <=9) && (minutes >=0 && minutes <=9) && (seconds >= 0 && seconds <= 9)){      
            
            //output 
            System.out.println("The time is: 0" + hours + " : " + "0" + minutes + " : " + "0" + seconds + '.');
        
        }//end if hours padded, minutes padded, seconds padded
        
        if((hours > 9 && hours <= 12) && (minutes >= 0 && minutes <=9) && (seconds >= 0 && seconds <=9)){
            
            //output
            System.out.println("The time is: " + hours + " : " + "0" + minutes + " : " + "0" + seconds + '.');
            
        }//end if hours fine, minutes padded, seconds padded
        
        if((hours > 9 && hours <= 12) && (minutes > 9 && minutes <= 59) && (seconds >=0 && seconds <=9)){
            
            //output
            System.out.println("The time is: " + hours + " : " + minutes + " : " + "0" + seconds + '.');
        }//end if hours fine, minutes fine, seconds padded
        
        if((hours >= 0 && hours <= 9) && (minutes >= 0 && minutes <= 9) && (seconds > 9 && seconds <=59)){
            
            //output
            System.out.println("The time is: 0" + hours + " : " + "0" + minutes + " : " + seconds + '.');
            
        }//end if hours padded, minutes padded, seconds fine
        
        if((hours >=0 && hours <=9) && (minutes > 9 && minutes <= 59) && (seconds > 9 && seconds <= 59)) {
            
            //output
            System.out.println("The time is: 0" + hours + " : " + minutes + " : " + seconds + '.');
            
        }//end if hours padded, minutes fine, seconds fine
        
        if((hours > 9 && hours <= 12) && (minutes >= 0 && minutes <= 9) && (seconds > 9 && seconds <= 59)){
            
            //output
            System.out.println("The time is: " + hours + " : " + "0" + minutes + " : " + seconds + '.');
            
        }//end if hours fine, minutes padded, seconds fine 
        
        if((hours >=0 && hours <=9) && (minutes > 9 && minutes <= 59) && (seconds >=0 && seconds <=9)){
            
            //output
            System.out.println("The time is: 0" + hours + " : " + minutes + " : " + "0" + seconds);
            
        }//end if hours padded, minutes fine, seconds padded
        
        if((hours > 9 && hours <= 12) && (minutes > 9 && minutes <= 59) && (seconds > 9 && seconds <= 59)){
            
            //output 2
            System.out.println("The time is: " + hours + " : " + minutes + " : " + seconds + '.');
            
        }//end if hours fine, minutes fine, seconds fine
        
    }//end main method
    
}//end class
