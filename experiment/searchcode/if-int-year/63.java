public class LeapYearMethod {
int inputYear;

public LeapYearMethod (int year) //constructor
{
this.inputYear = year;
}


public static boolean LeapYearCheck (int inputYear)
{
if (inputYear%4==0)
{
if ((inputYear%100==0 &amp;&amp; inputYear%400==0)|| (inputYear%100!=0 &amp;&amp; inputYear%400!=0))

