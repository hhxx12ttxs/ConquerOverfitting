public static double similarity(double[] v1, double[] v2) {
return 1 - distance(v1, v2)/Math.sqrt(2);
}

public static double distance(double[] v1, double[] v2) {
if(v1.length != v2.length) {
return -1;

