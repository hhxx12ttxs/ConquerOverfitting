String first = strs[0];
int endIndex = -1;
boolean loop = true;
while (loop &amp;&amp; ++endIndex < first.length()){
for(int i=1; i<strs.length; i++){
if(endIndex == strs[i].length() || strs[i].charAt(endIndex) != first.charAt(endIndex)){

