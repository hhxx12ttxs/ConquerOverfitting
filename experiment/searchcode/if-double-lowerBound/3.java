public static double doubleNumber(double upperBound, double lowerBound) {
double number;
Random random = new Random();

if (lowerBound != ZERO) {
upperBound = (upperBound+1) - lowerBound;
}

number = random.nextDouble() * upperBound;

if(number < lowerBound)

