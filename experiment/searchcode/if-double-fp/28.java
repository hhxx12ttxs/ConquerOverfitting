void addDigit(char c) {
if (c == &#39;.&#39;) {
fpMode = true;
} else {
addDigit( Character.digit(c, 10) ); // (int)c - (int)&#39;0&#39;
if (fpMode) {
fpDigits++;
val += (new Double(c)) / StrictMath.pow(10, fpDigits);

