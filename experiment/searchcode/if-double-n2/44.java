double n2 = m2/(m1*m1);
double n3 = m3/(m1*m2);
PH res = null;

int n = getSize(n2, n3);
if(n==0)return null;
else if(n == 1){
double[] alpha = {2/n2};
double[][] A ={{-1/m1}};

