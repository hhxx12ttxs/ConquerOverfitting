public class Binary{

public int rbsearch(int n, int[] L,int beg, int end){
int middle = (end-beg) / 2;
int end = L.length - 1;
int middle = (end-beg) / 2;
while(end != middle &amp;&amp; beg != middle){
if(L[middle] == n){

