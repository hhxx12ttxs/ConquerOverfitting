package jumpGameII;
public class JumpGameII{
public int jump(int[] A){
if(A == null || A.length <2) return 0;
int l = 0;
int r = 0;
int nSteps = 0;
while(r < A.length-1){
int max = r;

