public class ArithmeticCalculatorImpl implements ArithmeticCalculator {

@Override
public double add(double a, double b) {
double result = a + b;
System.out.println(a + &quot; + &quot; + b + &quot; = &quot; + result);
return result;
}

@Override
public double sub(double a, double b) {

