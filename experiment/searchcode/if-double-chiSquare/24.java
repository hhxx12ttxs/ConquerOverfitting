public static double Chi_square(int[] randomNums, int r)
{
double n_r = (double)randomNums.length / r*1.0;
double chiSquare = 0;

for(int i = 0; i < randomNums.length; i++){
double f = randomNums[i] - n_r;
chiSquare += f * f;

