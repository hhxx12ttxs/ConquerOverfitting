public double findMedianSortedArrays(int A[], int B[]) {
if (A.length==0 &amp;&amp;B.length==0){
return 0;
}
if (A.length==0 ||B.length==0){
int [] C=new int[A.length+B.length];
int a=0;
int b=0;
for (int c=0;c<C.length;c++){
if (a<A.length&amp;&amp;b<B.length){

