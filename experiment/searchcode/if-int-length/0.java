public class Solution {
public int removeDuplicates(int[] A) {
int length = 0;
for (int i = 0, j = 0; i < A.length; i = j) {
A[length ++ ] = A[i];
if (j - i >= 2)
A[length ++ ] = A[i];
}
return length;
}
}

