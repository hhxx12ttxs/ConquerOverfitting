static public Collection<Double> solve(double a, double b, double c) {
Collection<Double> ret = new TreeSet<Double>();
if (a == 0.)
ret.add(solve(b, c));
else {
double delta = b*b - 4*a*c;
if (delta == 0.)
ret.add(Double.valueOf(-b / (2. * a)));
else if (delta > 0.)

