public static Complejo suma(Complejo c1, Complejo c2) {
double x = c1.real + c2.real;
double y = c1.imag + c2.imag;
return new Complejo(x, y);
throws ExcepcionDivideCero {
double aux, x, y;
if (c2.modulo() == 0.0) {
throw new ExcepcionDivideCero(&quot;Divide entre cero&quot;);

