return random;
}

public static boolean hit(double probability) {
if (probability > 1 || probability < 0) {
double range = maxValue - minValue;
double randomValue = nextDouble() * range + minValue;
return randomValue;

