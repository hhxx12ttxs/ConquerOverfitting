private static int[] sort(int[] inp) {
int outerNum;
for(int idx = 1; idx < inp.length; idx++) {
outerNum = inp[idx];
for(int subIdx = idx-1; subIdx >=0; subIdx--) {
if(outerNum < inp[subIdx]) {

