long upperBound = (long)Math.sqrt((double)number);
long result = startValue;
if(upperBound <= startValue)
result = number;
for(long i = startValue;i<=upperBound;i++){
if(number%i == 0 &amp;&amp; isPrime(i)){
result = largestPrime(i,number/i);

