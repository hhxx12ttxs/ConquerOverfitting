double C1 = (A1*(x1+x2) + B1*(y1+y2))/2;
double C2 = (A2*(x2+x3) + B2*(y2+y3))/2;
double d = A1*B2-A2*B1;
if(d==0)return new double[]{0,0};
double y = (A1*C2-A2*C1)/d;
double x = -(B1*C2-B2*C1)/d;
return new double[]{x,y};
}

