if(num==null || num.length<3) return answer;

Arrays.sort(num);
int[] idx = new int[3];

while(idx[0] < num.length-2){

int restTarget = 0 - num[idx[0]];

