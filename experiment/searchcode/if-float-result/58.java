public static float[] multiply(float[] update, float scalar) {
float[] result = new float[update.length];
for (int i = 0; i < update.length; i++) {
result[i] = update[i] * scalar;
}
return result;
}

public static float[] subtract(float[] point, float[] point2) {

