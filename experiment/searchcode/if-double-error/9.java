double[] v = {3,2};
double[] z = {7.5,10};
double[] temp = new double[2];
double error = 0.00000000000001;
for (double beta=0; beta<=10; beta+=0.1) {
temp = add(scalarMult(alpha, u), scalarMult(beta, v));
if((temp[0] < z[0] + error &amp;&amp; temp[0] > z[0] - error) &amp;&amp; (temp[1] < z[1] + error &amp;&amp; temp[1] > z[1] - error))

