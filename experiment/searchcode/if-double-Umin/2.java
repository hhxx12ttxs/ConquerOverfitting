private double kd = 1.0;
private double n = 20;

private double ts;
private double lastSample;
private double uMin;
double[] dx = super.getRates(x, u), y = super.getOutput(x, u);

if(antiwindup) {
double v = (y[0] < uMin) ? uMin : (y[0] > uMax) ? uMax : y[0];

