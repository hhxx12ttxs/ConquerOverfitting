public void addFN() {
this.fn++;
}

public double getPrecision() {
if (tp == 0 &amp;&amp; fp == 0) {
return 0.0;
}
return ((double) (tp) / (double) (tp + fp)) * 100.0;

