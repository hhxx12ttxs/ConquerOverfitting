public int countGood(int[] superior, int[] workType) {
int N = superior.length;
int distinct = 0;

for(int i = 0; i < N; i++){
for(int j = i+1; j < N; j++){
if(superior[j] == i){
int type = workType[j];
if(used[type]){

