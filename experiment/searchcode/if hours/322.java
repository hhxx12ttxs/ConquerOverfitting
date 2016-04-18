/*----------------------------------------------------------------
 *  Author:        Wendy Wang
 *  Section:       K
 *  Written:       13 September 2014
 *
 *  Execution:     java Time
 *  
 *  Reads in integer command-line argument that represents elapsed
 *  minutes, then outputs start and finish time.
 *----------------------------------------------------------------*/

public class Time {
    
    public static void main(String[] args) {
        int elapsedMin, startHour, hours;
        elapsedMin = Integer.parseInt(args[0]);
        startHour = 12; //12pm
        hours = 0;
        
        String period = "pm"; //begin at noon
        
        for (int i = elapsedMin; i > 59; i -= 60) {
            hours++;
        }
        
        while (hours > 24) hours -= 24;
        int minutes = elapsedMin % 60;
        
        //corner cases: midnight and noon
        if (hours == 12) {
            period = "am";
        }
        if (hours == 24) {
            period = "pm";
        }
        
        //denoting am and pm
        if (hours < 12) {
            period = "pm";
            if (hours < 1) {
                hours = 12;
                period = "pm";
            }
        } else if (hours < 24) {
            period = "am";
        } else { 
            period = "pm";
        }
        
        // changing from 24hr to 12hr
        if (hours > 12) {
            hours = hours / 2;
        }
        
        String endTime = (hours + ":" + minutes + period);
        
        //formatting single-digit minutes
        if (minutes < 10) {
            endTime = (hours + ":0" + minutes + period);
        }
        
        System.out.println("Starting Time: 12:00pm");
        System.out.println("Finish Time:   " + endTime);
    }
}

