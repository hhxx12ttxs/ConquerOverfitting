public static int foo(int a[], int key, int p , int r){
if(p>r || a[p] > key ){
return -1;
}
else if(key > a[r]){
return r+1;
}
else
{
int mid = (p+r)/2;
System.out.println(p+ &quot; &quot;+ r);
if(key == a[mid] ||  (key> a[mid] &amp;&amp; key < a[mid+1])){

