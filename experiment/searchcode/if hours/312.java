public class MyTime {
	private int hours;
    private int minutes;
    private int seconds;



    public MyTime(int hours, int minutes, int seconds){
    	this.hours = hours;
    	this.minutes = minutes;
     	this.seconds = seconds;
    }

    
    public void sethours(int hours){
    	this.hours = hours;
    }

    public void setMinutes(int minutes){
    	this.minutes = minutes;
    }

    public void setSeconds(int seconds){
    	this.minutes = seconds;
    }

    public void advance(int h, int m, int s){

    	hours = hours + h;
    	hours = correctHours(hours);

    	minutes = minutes + m; 
    	

    	seconds = seconds + m;

    }

    public void reset(int h, int m, int s){
    	hours = h;
    	minutes = m;
    	seconds = s;
    }

    public int getHours(){
        return hours;
    }

    public int getMinutes(){
        return minutes;
    }

    public int getSeconds(){
        return seconds;
    }

    public int correctHours(int h){
    int m = 0;
    if (h%24 >= 1) {
    	m = 0 + h%24; 
    }
    return m;
    
    }

    public int correctMinutes(int m){
    int m = 0;
    if (m > 59) {
    	m = m - 60
    }
    return m;
    }
    

    public int correctSeconds(int s){
        int m = 0;
    if (h > 24) {
    	m = h - 24
    }
    if (h < 24) {
    	m = h;
    }
    return m;
    
    
    }
    public String toString(){
        return "The time is: " + hours + ":"+ minutes + ":" + seconds;
    }
}
