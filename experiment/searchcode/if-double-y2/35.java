double[] center(int x1, int y1, int x2, int y2, int x3, int y3){
double A1 = x1-x2;
double C1 = (A1*(x1+x2) + B1*(y1+y2))/2;
double C2 = (A2*(x2+x3) + B2*(y2+y3))/2;
double d = A1*B2-A2*B1;
if(d==0)return new double[]{0,0};

