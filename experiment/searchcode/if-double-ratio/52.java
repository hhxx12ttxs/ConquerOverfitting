public double maximalDistance(int[] velocities, int[] consumptions, int fuel) {
double bestRatio = 0.0;
double bestConsumption = 1.0;
double bestVelocities = 1.0;
for(int i = 0; i < velocities.length; i++) {
if((double)velocities[i]/(double)consumptions[i] > bestRatio) {

