private void expand() {
continuedFraction = new ArrayList();
if (fraction.getDenominator() == BigInteger.ONE)
continuedFraction.add( fraction.getNominator() );
else if (fraction.getNominator() == BigInteger.ONE) {

