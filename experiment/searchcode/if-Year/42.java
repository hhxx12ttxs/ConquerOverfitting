public static void main(String[] args) {
//int year = 2100;
if (year % 400 == 0){
if (year == 0)
System.out.println(year + &quot; is not a leap year.&quot;);
System.out.println(year + &quot; is a leap year.&quot;);
}
else if ((year % 4 == 0) &amp;&amp;(year % 100 != 0) ) {
System.out.println(year + &quot; is a leap year.&quot;);

