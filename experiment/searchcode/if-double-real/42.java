if (isZero()) {
return &quot;0.0&quot;;
}
if (Math.abs(real) > Double.MIN_VALUE) {
result += Double.toString(real);
}
if (Math.abs(imaginary) > Double.MIN_VALUE) {

