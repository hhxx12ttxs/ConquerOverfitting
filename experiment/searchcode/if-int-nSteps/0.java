package Recurse;

public class Staircase {
public int countWays(int nSteps){
if(nSteps<0){
return 0;
}
//one method to get to nSteps
if(nSteps == 0){
return 1;
}
return countWays(nSteps-1) + countWays(nSteps-2) + countWays(nSteps-3);

