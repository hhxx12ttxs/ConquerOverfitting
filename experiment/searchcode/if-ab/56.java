public static void main(String[] args) {
ArrayList<Integer[]> result = abResult(18,720);
for(Integer[] r: result) {
System.out.println(&quot;m value: &quot; + r[0] + &quot; n value: &quot; + r[1] + &quot; gcd(m,n): &quot; + gcd(r[0],r[1]) + &quot; lcm(m,n): &quot; + lcm(r[0],r[1]));
for(int i=gcd;i<=abProduct/2;i+=gcd) {
if (abProduct%i == 0) {
int j = abProduct/i;
if(gcd(i,j) == gcd) {

