
public class RotatedBinarySearch {

int rotated_binary_search(int A[], int N, int key){

int L = 0;
int R = N-1;

while(L<=R)
{
int M = L + (R-L)/2;
if(A[M] == key)
return M;

