*         describes the real/double coefficients of the polynomials.
*/
public class RealCoeff implements Coefficient {
public double coeff;

public RealCoeff(double coeff) {
this.coeff = coeff;
}

@Override
public char getSign() {
if (this.coeff < 0)
return &#39;-&#39;;

