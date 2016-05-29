int lowerbound = 1;      // Store the lowerbound
int upperbound = 1000;   // Store the upperbound
// Use a for-loop to repeatitively sum from the lowerbound to the upperbound
for (int number = 0; number <= upperbound; number++) {
if ((number%13==0) || (number%15==0) || (number%17==0) &amp;&amp; !(number%30==0))

