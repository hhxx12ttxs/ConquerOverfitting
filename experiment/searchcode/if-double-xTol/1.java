public class CrfLBFGS {

int m=5;
int[] iprint ;
boolean diagco;
double diag [ ];
double eps, xtol;

public void optimize ( int n ,  double[] x , double f , double[] g , int[] iflag){
if(diag==null){

