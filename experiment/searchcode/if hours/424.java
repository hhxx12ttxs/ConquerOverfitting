public class Time
{
   private int hours, minutes, seconds;

   Time() {
      hours = 0;
      minutes = 0;
      seconds = 0;
   }

   Time(int h, int m, int s) {
      if (h < 0 || h > 23)
         hours = 0;
      else
         hours = h;
      if (m < 0 || m > 59)
         minutes = 0;
      else
         minutes = m;
      if (s < 0 || s > 59)
         seconds = 0;
      else
         seconds = s;
   }
   
   Time(int h, int m) {
      if (h < 0 || h > 23)
         hours = 0;
      else
         hours = h;
      if (m < 0 || m > 59)
         minutes = 0;
      else
         minutes = m;
      seconds = 0;
   }
   
   Time(int h) {
      if (h < 0 || h > 23)
         hours = 0;
      else
         hours = h;
      minutes = 0;
      seconds = 0;
   }
   
   public String toString() {
      return "Hours: " + hours + "\n" +
             "Minutes: " + minutes + "\n" +
             "Seconds: " + seconds;
   }
   
   public String display() {
      String hrs = hours + "", min = minutes + "", sec = seconds + "";
      if (hours < 10)
         hrs = "0" + hours;
      if (minutes < 10)
         min = "0" + minutes;
      if (seconds < 10)
         sec = "0" + seconds;
      return hrs + ":" + min + ":" + sec;
   }

   public void setTime(int h, int m, int s) {
      if (h < 0 || h > 23)
         hours = 0;
      else
         hours = h;
      if (m < 0 || m > 59)
         minutes = 0;
      else
         minutes = m;
      if (s < 0 || s > 59)
         seconds = 0;
      else
         seconds = s;
   }
      
   
   public void setHours(int h) {
      if (h < 0 || h > 23)
         hours = 0;
      else
         hours = h;
   }

   public void setMinutes(int m) {
      if (m < 0 || m > 59)
         minutes = 0;
      else
         minutes = m;
   }
   
   public void setSeconds(int s) {
      if (s < 0 || s > 59)
         seconds = 0;
      else
         seconds = s;
   }
   
   public int getHours() {
      return hours;
   }
   
   public int getMinutes() {
      return minutes;
   }

   public int getSeconds() {
      return seconds;
   }
   
   public void increment(int amount) {
      minutes += amount;
      if (minutes > 59) {
         hours += (minutes / 60);
         minutes = (minutes % 60);
      }
   }

   public Boolean equals(Time t1) {
      int tseconds = (hours * 3600) + (minutes * 60) + seconds;
      int t1seconds = (t1.getHours() * 3600) + (t1.getMinutes() * 60) + t1.getSeconds();
      return tseconds == t1seconds;
   }
}
