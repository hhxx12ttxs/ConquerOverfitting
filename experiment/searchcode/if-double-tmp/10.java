static boolean inL2(double[] testMean,double[] projectMean,double value){
double tmp=0;
for(int i=0;i<projectMean.length;i++)
System.out.println(&quot;l2:tmp value&quot;+tmp+&quot; &quot;+value);
if (tmp<value)
return true;
else
return false;
}
static double[] L2Form(double[][] facebase,double[] face){

