while (r - l > 1e-10) {
double c1 = l + (r - l) / 3;
double c2 = l + (r - l) / 3 * 2;
double t1 = Math.sqrt(width[0] * width[0] + c1 * c1) / swim[0]
+ getMin2(length - c2, walk, width, swim);
if (t1 < t2)
r = c2;
else
l = c1;

