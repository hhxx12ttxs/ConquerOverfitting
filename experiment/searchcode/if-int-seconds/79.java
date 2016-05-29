for(int test: tests)
System.out.println(whatTime(test));
}


public String whatTime(int seconds)
{
if(seconds > 86399)
return &quot;You fail&quot;;

int hours = seconds / 3600;
seconds -= hours * 3600;

