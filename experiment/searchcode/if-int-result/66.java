public class Solution {
public int singleNumber(int[] A) {
int result=0;
if (A.length==0) return 0;
for (int i=0; i<A.length; i++){
result=result^A[i];
}
return result;

}
}

