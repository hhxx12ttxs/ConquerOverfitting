long num = 1;
if(digits == null)
return null;
int len = digits.length;

for(int i=0; i<len; i++)
int strLen = str.length();
int[] res = new int[strLen];
for(int i=0; i<strLen; i++)
{
res[i] = Character.getNumericValue(str.charAt(i));

