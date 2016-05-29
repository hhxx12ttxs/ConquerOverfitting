double savedp = 0;
double ret = n;

while (true) {
if(n%2 == 0) {
if(savedp != 2) {
break;
}
}
for(double k = 3; k<=Math.sqrt(n); k+=2) {

if(n%k == 0) {
if(savedp != k) {

