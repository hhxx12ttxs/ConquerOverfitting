package ifelse;

public class LeapYear {
int year;
String yearStr;

public int getYear() {
public String getYearStr() {
return yearStr;
}
public void setYearStr() {
if(year %4==0 &amp;&amp; year%100 !=0 || year%400==0)//4로 나눠 떨어진다.

