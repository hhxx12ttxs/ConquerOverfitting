public static Complex onPoint(SL2C t, Complex z){
if(z.isInfinity()){
if(!t.c.isZero()){
return Complex.div(t.a, t.c);
}else{
Complex numerix = Complex.add( Complex.mult(t.a, z), t.b);
Complex denominator = Complex.add( Complex.mult(t.c, z), t.d);

if(denominator.isZero()){

