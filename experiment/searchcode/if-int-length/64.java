public class Solution {
public int removeDuplicates(int[] A) {
if(A.length<1)
return 0;

int i=0, j=0;
for(; j<A.length; j++){
if(A[i] != A[j])
A[++i] = A[j];
}
return i+1;
}
}

