public static Random random = new Random();

public static int getInt(int lowerBound, int upperBound) {
if (lowerBound == upperBound) {
return random.nextInt(upperBound - lowerBound) + lowerBound;
}

public static double getDouble(double lowerBound, double upperBound) {
return random.nextDouble() * Math.abs(upperBound - lowerBound) + Math.min(lowerBound, upperBound);

