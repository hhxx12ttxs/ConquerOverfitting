package prac.test;import java.util.*;

public class TimeConversion {

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String time = in.next();
        String newtime = "";
        String[] parts = time.split(":");
        int hours = Integer.valueOf(parts[0]);
        String hrs = "";
        
        String secs = parts[2];
        
        System.out.println(hours);
        if(secs.contains("PM"))
        {
            hours = hours+12;
            
            if(hours==24)
                hours=12;
            
            hrs = String.valueOf(hours);
        }        
        else{
            int temp = hours+12;
            if(temp==24)
            {
            	hours=0;
            	hrs = "00";
            }
            else{
            	hrs = String.valueOf(hours);
            	if(hrs.length()==1)
            		hrs="0"+hrs;
            }
            System.out.println(hours);
          
        }
        newtime = hrs+":"+parts[1]+":"+secs.substring(0,secs.length()-2);
        System.out.println(newtime);
    }
}

