public poisson(randomNum aRNG){
random = aRNG;
}
public  int poission(double xm){
double em,t,y;
oldm = -1.0;
if(xm<12.0){
if(xm!=oldm){
oldm = xm;
g = Math.exp(-xm);
}
em = -1.0;

