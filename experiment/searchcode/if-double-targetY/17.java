public double[] bulletPos(double targetX, double targetY) {
double[] bPos = new double[2];
double diffX = targetX - pos[0];
double diffY = targetY - pos[1];
double diff = Math.sqrt( Math.pow(diffX, 2) +

