int day= 8;

int dayOfWeek= (day+ month+ year+ (year/4) +6) %7;

if (dayOfWeek==0){
System.out.println(&quot;sunday&quot;);
}
else if (dayOfWeek==1){
System.out.println(&quot;monday&quot;);

