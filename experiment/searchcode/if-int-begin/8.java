public int partition( int a[], int begin, int end ){
int pivot = a[begin];
while( begin < end ){
while( begin < end &amp;&amp; a[end] >= pivot ) end--;
return begin;
}
public void quickSort( int a[], int begin, int end ){
int pivotPos;
if( begin < end ){

