* Created by haha on 2015/4/27.
*/
public class Solution {
public List<Integer> getRow(int rowIndex) {
int[] res = new int[rowIndex / 2 + 1];
res[0] = 1;
for(int r = 1; r <= rowIndex; ++r){
if(r % 2 == 0){

