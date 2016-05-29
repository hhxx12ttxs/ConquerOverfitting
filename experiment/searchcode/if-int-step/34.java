for (int j = 0; j < i; j++){
if (can[j] &amp;&amp; j + A[j] >= i){
can[i] = true;
break;
*  answer : step[n - 1]
*/

public int jump(int[] A) {
int step[] = new int[A.length];
step[0] = 0;

