package anna8;

public class Metropolis {
Phi phi;
double x;
double delta;

public Metropolis(Phi phi, double x0, double delta) {
double deltaN = Math.random() * 2 * delta - delta;
double xTrial = x + deltaN;
double w = phi.phi2(xTrial) / phi.phi2(x);
if (w >= 1) {

