/**
* Recursion Version
*/
public class Step{
public static int stepCount(int n){
if(n < 0) return 0;
else if(n == 0) return 1;
else {
return stepCount(n-1) + stepCount(n-2) + stepCount(n-3);

