public static double scalarProduct(double[] v1,double[] v2){
double res=0;
if(v1.length!=v2.length)
throw new IllegalArgumentException();
for(int i=0;i<v1.length;i++){
public static double[] add(double[] v1,double[] v2){
if(v1.length!=v2.length)
throw new IllegalArgumentException();
double[] res=new double[v1.length];

