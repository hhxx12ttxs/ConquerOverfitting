String [] beforeToneMarks = before.trim().split(&quot; &quot;);
StringBuffer result = new StringBuffer();
for (int i=0; i<beforeToneMarks.length; i++){
String tmp = beforeToneMarks[i].trim().toLowerCase();
if (tmp.indexOf(&quot;a&quot;) >=0){
char tone = tmp.charAt(tmp.length()-1);

