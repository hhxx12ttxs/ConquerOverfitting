if(n == 0)
break;
int idx = Arrays.binarySearch(mid, n);
if(idx >= 0) {
System.out.println(idx + &quot; &quot; + idx);
//System.out.println(&quot;index: &quot; + idx);
int num = idx, den = idx;
if(idx%2 == 0) {
if(n-mid[idx] <= idx-1) {

