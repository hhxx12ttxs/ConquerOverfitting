for(int i = 1; i < Y.length-1; i++){
range = getRange(Y[i], Y[i+1], range);
if(range[0] != Integer.MIN_VALUE){
temp++;
temp = 1;
}
}
return max;
}

private int[] getRange(int x, int y){
int[] A = new int[2];
if(x < y){

