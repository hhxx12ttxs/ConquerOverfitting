//check each adjoining pairs in assigned if it is not continuous then
//e.g. is assigned[1] = assigned[0]+1
//assigned is designed so that each number is at it&#39;s n-1 slot
public int highestPval(int x, int y){
int retval = 0;
ArrayList<Integer> pval = map[x][y].possible;

if(!pval.isEmpty()){
//int length = pval.size();

