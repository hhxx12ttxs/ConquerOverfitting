private static Random javaRandom = new Random();

public static double randomFloat() {
return javaRandom.nextFloat();
}

public static double random(float min, float max) {
randomDouble *= (double) (max - min + 1);
long randomValue = (long) Math.floor(randomDouble);
if (randomValue > max) {

