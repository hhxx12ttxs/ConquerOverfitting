public static void main(String[] args) {
int i = 144;
while(true){
long x = i*(2*i-1);
double inv = (Math.sqrt(24*x+1.0)+1.0)/6.0;
if(inv == (int)inv){
System.out.println(x);
System.out.println(i);

