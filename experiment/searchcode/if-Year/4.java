public LeapYear(int year)
{
this.year = year;
}

public boolean isLeapYear()
{
if (year % 4 == 0 &amp;&amp; year < 1582)
return true;

else if (year > 1582)
{
if (year % 400 == 0 )
return true;

