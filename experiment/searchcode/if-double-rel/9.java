package conversions.peaks;

public class Peak {
public double power;
public double powerRel;
public Peak(double pow, double powNoise, double center, double width) {
this.power = pow;
this.powerRel = pow - powNoise;
if (this.powerRel < 1.0e-9) {

