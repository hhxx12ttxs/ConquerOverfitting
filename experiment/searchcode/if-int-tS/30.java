public double closestToAverage(double [] ts) throws TemperatureException {
if (ts.length == 0) return 0;
double avg = 0;
for (int i = 0; i < ts.length; i++) {
double absClosest = Double.MAX_VALUE;
double closest = 0;
for (int i = 0; i < ts.length; i++){
if (ts[i] < -273 || ts[i] > 5526) throw new TemperatureException();

