return (findKth(nums1, 0, nums2, 0, n/2) + findKth(nums1, 0, nums2, 0, n/2 + 1))/2.0;
}
}
private int findKth(int[] A, int startA, int[] B, int startB, int k){
if(startB >= B.length) return A[startA + k - 1];
if(k == 1) return A[startA] < B[startB] ? A[startA] : B[startB];

int currA = startA + k/2 - 1 < A.length ? A[startA + k/2 - 1]:Integer.MAX_VALUE;

