public static double[] multiply( double[] X, double Y){
double[] retVal = new double[X.length];
for (int i = 0; i < retVal.length; i++){
public static double[] multiply( double[] X, double[] Y){
if( X.length != Y.length )
throw new Error(&quot;vectors sizes differ&quot;);

