public class Solution {
public int removeElement(int[] A, int elem) {
int count=0;
for(int i=0;i<A.length;i++)
if(A[i]==elem)
count++;
else{
if(count>0)

