public class Year
{
private int year = 0;
public Year(int year){
this.year = year;
}
public boolean isLeapyear(int year)
{
if ((year%100 == 0 &amp;&amp; year%400 == 0)||(year%4 == 0 &amp;&amp; year%100 != 0))

