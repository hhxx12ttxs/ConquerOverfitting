public NumMonth(int num)
{
n = num;
}

public String getDays()
{
String a = &quot; &quot;;

if (n == 1)
a = &quot;31 days&quot;;
else if (n == 2)
a = &quot;28 days&quot;;
else if (n == 3)
a = &quot;31 days&quot;;
else if (n == 4)
a = &quot;30 days&quot;;

