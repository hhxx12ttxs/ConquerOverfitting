double pi = Math.PI;
double mu_r = pi/4;
double R2 = 1.0;
double R1 = 0.5;
double theta = Math.atan2(x[1],x[0]);

double[][] J = {{1,0},{0,1}};
int ri = 0;
if (r>R2){ri = 1;}
else { if (r<R1){

