double c = Double.parseDouble(args[2]);

double disc = b*b - 4*a*c;

if (disc > 0.0) {
double r2 = (-b - Math.sqrt(disc))/(2.0*a);
StdOut.println(r1 + &quot; &quot; + r2);
} else if (disc < 0.0) {
double re = -b/(2.0*a);

