return xMax / xMin;
}
}

public double heightFactor() {
if (yMax - yMin == 0) {
return 0;
}
if (yMax < 0 &amp;&amp; yMin < 0) {
public double yFactor(double height) {
if (yMax - yMin == 0) {
return 0;
}
return height / (yMax - yMin);
}

public double xFactor(double width) {

