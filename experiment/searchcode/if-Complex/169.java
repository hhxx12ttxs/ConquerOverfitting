double denom = this.a*this.a + this.b*this.b;
if(denom==0) return new Complex(Double.NaN,Double.NaN);
return new Complex( (in.a*this.a + in.b*this.b) / denom , (in.b*this.a - in.a*this.b) / denom );
public Complex[] root(int in){
if(in==0) return new Complex[] {new Complex(1,0)};
else if(in<0) return this.pow(new Frac(-1,Math.abs(in)));
public Complex(double a, double b){ this(a, b, false); }
/** Constructs the new complex number in polar form if isPolar==true */
public Complex(double r, double phi, boolean isPolar){
public Complex root_f(int in){
if(in==0) return new Complex(1,0);
else if(in<0) return this.pow_f(new Frac(-1,Math.abs(in)));
double denom = in.a*in.a + in.b*in.b;
if(denom==0) return new Complex(Double.NaN,Double.NaN);
else return new Complex( (in.a*this.a + in.b*this.b) / denom , (this.b*in.a - this.a*in.b) / denom );

