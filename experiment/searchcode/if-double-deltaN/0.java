this.delta = delta;
}

double step() {
double deltaN = Math.random() * 2 * delta - delta;
double xTrial = x + deltaN;
double w = phi.phi2(xTrial) / phi.phi2(x);
if (w >= 1) {

