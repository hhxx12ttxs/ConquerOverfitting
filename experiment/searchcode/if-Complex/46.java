public ComplexRGBTexture threshold( int cut ) {
for (complex z : data) if (complex.mod(z) < cut) z.setComplex(0,0);
return new Complex(Re/divisor,-Im/divisor);
}

public Complex complexPolarPow(int n){

if (n == 0 || n < 0)

