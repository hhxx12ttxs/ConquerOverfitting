public class LeapYear {
public static void main(String[] args) {
int year = 2004;
if (year%400 == 0) {
System.out.println(year+&quot; is a leap year.&quot;);
} else if ((year%4 == 0)||(year%100 == 0)) {
System.out.println(year+&quot; is a leao year..&quot;);
}
}
}

