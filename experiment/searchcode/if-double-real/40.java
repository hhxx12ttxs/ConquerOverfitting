public static String sua_v(double s, double u, double a){
double d=Math.sqrt(u*u+2*a*s);
if(Double.isNaN(d)){
double d=((Math.sqrt(u*u+2*a*s))-u)/a;
if(Double.isNaN(d)){
return &quot;=\\mathrm{No\\ real\\ solution\\\\ square\\ root\\ of\\ a\\ negative}&quot;;

