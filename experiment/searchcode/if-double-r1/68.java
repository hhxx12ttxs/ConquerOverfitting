double c = input.nextDouble();

double discriminant = b * b - 4 * a * c;

if (discriminant < 0) {
Complex r1 = new Complex(-b / (2 * a), Math.pow(-discriminant, 0.5) / (2 * a));
System.out.println(&quot;The roots are &quot; + r1 + &quot; and &quot; + r2);
}
else if (discriminant == 0) {
double r1 = -b / (2 * a);

