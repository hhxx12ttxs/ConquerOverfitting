//http://community.topcoder.com/stat?c=problem_statement&amp;pm=1172&amp;rd=4701
public class MandelBrot
{
public int iterations(int max, double a, double b)
{
double realPart = a;
double imaginaryPart = b;
double currentResult = Math.sqrt(a*a + b*b);

