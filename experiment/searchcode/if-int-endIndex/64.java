static void replaceSpaces(char[] str, int length){
int spaceCount = 0;
int endIndex;
str[endIndex] = &#39;\0&#39;;
for(int i = length - 1; i >= 0; i--){
if(str[i] == &#39; &#39;){
str[endIndex - 1] = &#39;0&#39;;

