public class Solution {
public int sqrt(int x) {
if(x<=1) {
return x;
}

int begin = 1;
int end   = x;
int middle = 0;
while(begin<=end) {

