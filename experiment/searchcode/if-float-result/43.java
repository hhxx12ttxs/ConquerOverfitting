static float[][] deepCopy(float[][] input) {
if (input == null)
return null;
float[][] result = new float[input.length][];
result[r] = input[r].clone();
}
return result;
}

static double sumArray(float[][] array2) {
double result = 0;

