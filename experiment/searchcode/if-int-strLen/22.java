for(int j=999; j>=100; --j)
{
int k = i*j;
if(isPalindrome(k))
{
if(k>maxpal)
maxpal = k;
String numString = new Integer(num).toString();
int strlen = numString.length();
for(int i=0; i<strlen/2; ++i)
{
if(numString.charAt(i)!=numString.charAt(strlen-1-i))

