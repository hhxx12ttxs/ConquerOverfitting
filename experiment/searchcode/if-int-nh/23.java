public int chop(int search,int[] array) {
for(int i=0; i<array.length ;i++) {
if(search == array[i]){
return i;
int n = array.length - 1;
int nh = n;
int nl = 0;
int nm,v;

while(nl <= nh) {
//get average(nh, nl)
nm = (nh+nl) >>> 1;

