// standard set and get function;

public class CarState {
protected double[] s;

public CarState () {
s = new double[3];
adjustTheta();
}

// Don&#39;t let theta exceed 2PI
private void adjustTheta() {
s[2] = s[2] % (2 * Math.PI);
if (s[2] < 0.0) {

