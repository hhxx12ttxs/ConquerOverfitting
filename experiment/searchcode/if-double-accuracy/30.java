package mes;

public class GS {
private static int accuracy=1000;

public static void setAccuracy(int a){
accuracy=a;
}

public static double[] solve(double[][] A, double[] b) {
int n=A.length;

