double X = Math.log(x);
double delta = 0.00001;

while(start <= end) {
int mid = (start + end) / 2;
double M = 2 * Math.log(mid);

if(Math.abs(M - X) < delta) {

