// Ex 8.15: Rational numbers.

public class Rational {
private int num;
private int den;

public Rational(int num, int den) {
if (den == 0)
throw new IllegalArgumentException(

