public StretchTransform2D(final double xCoeff, final double yCoeff) {
if (Math.abs(xCoeff) <= Double.MIN_NORMAL) {
throw new IllegalArgumentException(&quot;xCoeff must be positive&quot;);
}
if (Math.abs(yCoeff) <= Double.MIN_NORMAL) {
throw new IllegalArgumentException(&quot;yCoeff must be positive&quot;);

