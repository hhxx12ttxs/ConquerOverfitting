while(i<s.length()&amp;&amp;Character.isSpace(s.charAt(i))) ++i;
int start=i;
if(start==s.length()) break;
//end is space or s.length()

//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
if(ret==&quot;&quot;)
ret=s.substring(start,end);

