for(int i=0;i<8000;i++){
double c=0.001*i-4;
double a1=Math.exp(-c);
double a2=Math.exp(c);
tanh_table[i]=(a2-a1)/(a2+a1);
}
}
public double exp(double value){

