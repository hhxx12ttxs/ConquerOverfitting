public void run()
{
println(&quot;Leap Year Test Program&quot;);
int year = readInt(&quot;What year would you like to check? &quot;);
if (isLeapYear(year))
println(&quot;not a leap year because year modulus 400 equals &quot; + year % 400);
}

}

private boolean isLeapYear(int year)

