protected double random(double rangeMin, double rangeMax) {
Random r = new Random();
double randomValue = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
return randomValue;
}

protected int activate(double sum) {
//Return a 1 if positive, -1 if negative.

