System.out.println(&quot;enter year&quot;);
int year;
year=Integer.parseInt(args[0]);
if(year%100==0)
{
if(year%400==0)
System.out.println(+year+&quot; is not a leap year&quot;);
}
}
else
{
if(year%4==0)
{

System.out.println(year +&quot; is a leap year&quot;);

