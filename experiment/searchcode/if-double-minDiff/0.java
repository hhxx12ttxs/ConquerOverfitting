if(isPentagonal(m + n) &amp;&amp; isPentagonal(m - n)){
if((m - n)<minDiff){
minDiff = m-n;
k = i; l = j;
}
}
if((minDiff > (m - n)) &amp;&amp; (j == i -1) &amp;&amp; (minDiff != Integer.MAX_VALUE)){

