private static final double G = M * K;
private double upperBound;
private String label;
private double constant;

public NumberFormatLabel(double upperBound, boolean inBits) {
private void calculateLabel(double upperBound) {
if (upperBound > 1024 &amp;&amp; upperBound < M) {
label = KB;

