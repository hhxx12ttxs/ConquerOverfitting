public class T1Q28{
public static void run()
{
double a = 1.1;
double b = 1.2;
double a1 = (a+b)*(a-b);
double a2 = (a*a) - (b*b);
if((a+b)*(a-b) != (a*a) - (b*b)) System.out.printf(&quot;\nMathematical error!  \n%.91f\n%.91f\n&quot;, a1, a2);
}
}

