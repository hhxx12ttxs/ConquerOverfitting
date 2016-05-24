else {
// The number can be in a decimal format or complex format
if (!isRealNumber(number) &amp;&amp; !isComplexNumber(number)) {
// This is because there is a bug parsing complex number. 9i is parsed as 9
if (complex.getImaginary() == 0 &amp;&amp; value.contains(&quot;i&quot;)) isComplex = false;

