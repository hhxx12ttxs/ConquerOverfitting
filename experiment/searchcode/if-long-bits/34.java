while(t-->0) {
String s = input.readLine();
long n = Long.parseLong(s);
System.out.println(flipBits(n));
int len = binary.length();
int bits = 0;
long ans = 0;
while(bits < len) {
if(binary.charAt(len - 1 - bits) == &#39;0&#39;) {

