int p1 = m-1;
int p2 = n-1;
int k = m+n-1;
while(p1>=0 &amp;&amp; p2>=0){
if(nums1[p1]>nums2[p2]){
else{
nums1[k--] = nums2[p2--];
}
}
// As long as there is elements being saved from num1 in previous run, p1 will be < 0

