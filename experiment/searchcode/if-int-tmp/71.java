public int lengthOfLongestSubstring(String s) {
int rtRes = 0;
String sTmp = s;
int tmpCount = 0, breakIndex =0;
String sTmpChk =&quot;&quot;;


for(int i=0;i<sTmp.length();i++){

sTmpChk = sTmp.substring(i,i+1);

