package mwang;

public class LeapYear
{
private int year;

public LeapYear(int year)
{
this.year = year;
}

public boolean isLeapYear()
{
if (year % 4 == 0 &amp;&amp; year < 1582)

