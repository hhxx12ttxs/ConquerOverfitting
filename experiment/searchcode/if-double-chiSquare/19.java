double[] O = new double[classes];
double chiSquare = 0;

double classSize = (maxClass-minClass)/classes;
chiSquare = chiSquare + Math.pow((O[i]-E[i]), 2)/E[i];

return chiSquare;
}
//	public static String goodnessOfFitTest(int degOfFreedom, double chiSquare) {

