ArrayList<Double> results = new ArrayList<Double>();
if (getDegree() == 4) {
double c4 = coefs.get(4);
double c3 = coefs.get(3) / c4;
if (t2 >= -TOLERANCE) {
if (t2 < 0.0)
t2 = 0.0;
t2 = 2 * Math.sqrt(t2);
double t1 = 3 * c3 * c3 / 4 - 2 * c2;

