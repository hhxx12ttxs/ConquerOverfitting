public double[] calculaBaskara(double a, double b, double c) throws Exception{
double[] resultado = new double[3];
double delta;

if(a==0){
throw new Exception(&quot;O valor de A deve ser maior que 0&quot;);
}

delta = (Math.pow(b, 2))-(4*a*c);

