public class CauchyDecider extends FailureDecider {
private double mymu;

private double mysd;

public CauchyDecider(double mu, double sd) {
double AT9 = -0.036531572744216916;
double AT10 = 0.016285820115365782;
boolean negative = x < 0;
if (negative)

