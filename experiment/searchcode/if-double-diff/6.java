float[] finalHSB = { 1, 1, 1};
double diff;

public NonLinearHSV(double min, double max) {
super(min, max);
this.diff = max - min;
}

@Override
public Color getColor(double value) {
if(value > max) {

