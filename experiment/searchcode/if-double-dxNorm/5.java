else  return x1 + step()*d1();
}
public double x2New() {
if(!is2)return x2 + d2();


else   return x2 + step()*d2();
}
public double dxNorm() {
double a = x1New() - x1;

