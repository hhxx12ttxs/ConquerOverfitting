public boolean isPalindrome(int x) {
if(x<0)
return false;
int end = 0;
int compare = x;
while(compare!=0){
end = end*10 + compare%10;

