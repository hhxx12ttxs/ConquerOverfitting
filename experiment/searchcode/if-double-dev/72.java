public class Data {
double value;
double stdDev;

public Data(double[] data) {
this.value = getMean(data);
this.stdDev = getStdDev(data);
}

public static double getStdDev(double[] data) {
if (data.length == 0) {

