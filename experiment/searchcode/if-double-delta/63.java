double C=Double.parseDouble(args[2]);
double delta=B*B-4*A*C;
if(A==0){ double x=-C/B; System.out.println(x); }
else {
if(delta>=0) { double x1=(-B+Math.sqrt(delta))/(2*A); double x2=(-B-Math.sqrt(delta))/(2*A); System.out.println(x1+&quot;\n&quot;+x2);}

