R75=-0.539822500731772,R76=0.801422961990337,R77=-0.257512919478482;
int nh,i,j;
if (n < 8) return;
double[] wksp = new double[n];
nh = n >> 1;
if (isign >= 0) {
wksp[0]  = R00*a[0]+R01*a[1]+R02*a[2];

