public static double divide(double d,double e){

if(d<e){
return 0;
}
double powerOfTwo = 0;
int count = 0;
while(powerOfTwo*e < d){
double newPowerOfTwo = Math.pow(2, count);
if( newPowerOfTwo* e < d) {
powerOfTwo = newPowerOfTwo;

