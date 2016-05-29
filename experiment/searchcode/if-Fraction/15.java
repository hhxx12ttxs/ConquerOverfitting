for (int num = startNum; num <= endNum; num++) {
Fraction fraction = new Fraction(num, d);
if (Utils.gcd(fraction.getNum(), fraction.getDenom()) == 1) {
if (fraction.compareTo(low) > 0 &amp;&amp; fraction.compareTo(high) < 0) {

