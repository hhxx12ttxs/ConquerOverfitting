public class Pdf2step implements Function1D {

private int dim;

public Pdf2step(){

Pdf2stepConstructor();
return;
public double eval(double x) {

if(dim==3){
return x*x <= 1 ? 1 : 0;
}
else if(dim==1){
return x*x == 1 ? 1 : 0;

