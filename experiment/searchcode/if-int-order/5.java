doit(winner, order, order.length, result);
}

public static void doit(int [][] winner, int [] order, int len, int[] result){
if (len == 1) {
result[0] = order[0];
return;
}
for(int i = 0; i<len/2; i++){
if( winner[order[2*i]][order[2*i+1]] == order[2*i]){

