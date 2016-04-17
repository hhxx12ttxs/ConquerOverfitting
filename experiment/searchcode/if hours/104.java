import java.util.*;

public class Clock {
  private int hours;
  private int minutes;
  private String ampm;
  
  Clock(){
    hours = 12;
    minutes = 0;
    ampm = "am";
  }
  
  Clock(int h, int m, String ap){
    hours = h;
    minutes = m;
    ampm = ap;
  }
  
  void tick(){
    minutes = minutes + 1;
    if ( hours == 11 && minutes == 60 ){
      if (ampm == "am")
        ampm = "pm";
      else if (ampm == "pm")
        ampm = "am";
    }
    if ( minutes == 60 && hours == 12 ){
      minutes = 0;
      hours = 1;
    }
    else if ( minutes == 60 ) {
      minutes = 0;
      hours = hours + 1;
    }

  }
  

  public String toString(){
    if (minutes >= 0 && minutes <= 9){
     String s = hours + ":0" + minutes + " " + ampm;
     return s;
    }
    else{
     String s = hours + ":" + minutes + " " + ampm;
     return s;
    }
  }
    
}
