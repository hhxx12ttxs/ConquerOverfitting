public class LeapYear{

public int year;
public LeapYear(int x){
year = x;
}

public String ISLeapYearQ(){
if((year % 400) == 0){
return &quot; is &quot;;
}
else if(((year % 4) == 0) &amp;&amp; ((year % 100) != 0)){

