for(int i=0;i<8000;i++){
double c=0.001*i-4;
double a1=Math.exp(-c);
double a2=Math.exp(c);
for(int j=0;j<a2.length;j++)
G[i][j]=a1[i]*a2[j];
return G;
}

public double tanh(double a){
/*
if(a>-4&amp;&amp;a<4)

