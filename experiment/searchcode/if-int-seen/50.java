/* Seems that xor is the solution that requires no extra space and has linear time */
/* Add to hashset if not seen. Remove from hashset if seen. Return what&#39;s left in the set */
HashSet<Integer> seen = new HashSet<Integer>();
for (int i = 0; i < A.length; i++) {
if (seen.contains(A[i]))

