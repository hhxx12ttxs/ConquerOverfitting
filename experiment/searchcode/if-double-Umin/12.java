private double n = 20;

private double ts;
private double lastSample;
private double uMin;
private double uMax;
double[] dx = super.getRates();

if(antiwindup) {
double v = (y[0] < uMin) ? uMin : (y[0] > uMax) ? uMax : y[0];

