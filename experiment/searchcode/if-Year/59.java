package leapyear1;
public class year {
public int year;

public  int getYear() {
return year;
setYear(year);
}

public  year(){};

public  boolean isLeap(){
boolean result;
if((this.getYear()%4==0)&amp;&amp;(this.getYear()%100!=0)){

