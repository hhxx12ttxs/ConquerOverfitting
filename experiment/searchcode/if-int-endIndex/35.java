public String longestCommonPrefix(String[] strs) {
int endIndex = 0;
boolean flag = true;
if(strs == null || strs.length < 1 || strs[0].isEmpty())
char c = strs[0].charAt(endIndex);
for(int i=1;i<strs.length;i++) {
String s = strs[i];
if(s.length()-1 < endIndex) {

