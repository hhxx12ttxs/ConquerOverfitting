private static void LargestPrimeFactor(double number){
double largestFactor = 0;
for(double i = 0; i < number; i++){
if(number % i == 0 &amp;&amp; isPrime(i)){
System.out.println(largestFactor);
}

boolean isPrime (double factor) {
if(factor % 2 == 0) return false;
for(var i = 3; i*i <= factor; i+= 2){

