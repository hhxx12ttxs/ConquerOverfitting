return zero;
}


public static double[] add(double[]v1, double[]v2 ){
if(v1.length!=v2.length)return null;
for(int i=0; i<v1.length ; i++)v1[i]+=v2[i];
for(int i=0; i<v1.length ; i++)v1[i]=v1[i]/divisor;

return v1;
}

public static double dot(double[]v1, double[]v2 ){
if(v1.length!=v2.length)return -10;

