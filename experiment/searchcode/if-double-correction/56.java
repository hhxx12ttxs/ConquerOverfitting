public double calculateDistance(int txPower, double rssi) {
if (rssi == 0) {
return -1.0D;
}

double ratio = rssi / txPower;
double rssiCorrection = 0.96D + Math.pow(Math.abs(rssi), 3.0D) % 10.0D / 150.0D;

