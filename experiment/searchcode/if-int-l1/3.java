public static boolean nextComb(int[] l1, int cap){
for (int ptr = l1.length-1;;){
if (ptr < 0) return false;
if (l1[ptr] == cap - 1){
for (int i = l1.length- 2; i >=0; i--) {
if (l1[i] < l1[i + 1]){
for (int k = l1.length - 1; k>=0;k--){

