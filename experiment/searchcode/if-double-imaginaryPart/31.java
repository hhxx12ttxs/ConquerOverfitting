this.realPart = BigDecimal.ZERO;
this.imaginaryPart = BigDecimal.ZERO;
this.undefined = true;
} else if (Double.isInfinite(real) || Double.isInfinite(imaginary)) {
public double[] toDoubles() {
if (realPart == null || imaginaryPart == null) return new double[]{Double.NaN, Double.NaN};

