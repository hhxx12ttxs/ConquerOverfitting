class Solution {
public:
vector<int> getRow(int rowIndex);
};

vector<int> Solution::getRow(int rowIndex){
vector<int> result(rowIndex+1,1);
if(rowIndex==0 || rowIndex == 1) return result;

for(int i=2;i<=rowIndex;i++){

