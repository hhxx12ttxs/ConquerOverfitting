* For example, given k = 3,
* Return [1,3,3,1].
**/

public class Solution {
public List<Integer> getRow(int rowIndex) {
for(int i=0;i<=rowIndex;i++){
for(int j=0;j<=i;j++){
if(j==0||j==rowIndex){

