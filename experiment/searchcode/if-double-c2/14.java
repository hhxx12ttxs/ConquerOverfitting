while (r - l > 1e-10) {
double c1 = l + (r - l) / 3;
double c2 = l + (r - l) / 3 * 2;
double t1 = Math.sqrt(width[0] * width[0] + c1 * c1) / swim[0]
+ getMin2(length - c1, walk, width, swim);
double t2 = Math.sqrt(width[0] * width[0] + c2 * c2) / swim[0]
+ getMin2(length - c2, walk, width, swim);

