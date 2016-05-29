while (p >= 0 &amp;&amp; p1 >= 0 &amp;&amp; p2 >= 0) {
if (nums1[p1] > nums2[p2]) {
nums1[p--] = nums1[p1--];
} else {
nums1[p--] = nums2[p2--];
}
}

if (p1 < 0) {

