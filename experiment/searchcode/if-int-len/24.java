public int selectGasStation(int[] a, int[] g) {
int len = a.length;
if(len == 0) return 0;
int[] c = new int[2*len];
int sum = 0;
for(int i=0;i<2*len;i++){
// c[i] not a[i]
sum += c[i];
if(sum < 0){

