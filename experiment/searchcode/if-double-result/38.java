public class ArithmeticCalculatorImpl implements ArithmeticCalculator {

public double add(double a, double b) {
double result = a + b;
return result;
}

public double div(double a, double b) {
if (b == 0) {
throw new IllegalArgumentException(&quot;Division by zero&quot;);

