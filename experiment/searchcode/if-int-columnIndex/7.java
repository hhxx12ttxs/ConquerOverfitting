for(int i = 0; i < columnIndex.length; i++) {
for(int j = i+1; j < columnIndex.length; j++) {
if((columnIndex[j] - columnIndex[i] == j-i) ||(columnIndex[j] - columnIndex[i] == i-j) ) {
public static void eightQueen(int[] columnIndex, int first, int last) {
if(first == last ){
if(check(columnIndex)){

