boolean past = diff >= 0;
if( ! past )  diff = -diff;
if( diff < 10 )  return &quot;now&quot;;

boolean sameDay = now.getDate() == when.getDate();
String ago = past ? &quot; ago&quot; : &quot;&quot;;

long diff = diffSec;
if( diff <     30 )  return in + &quot;few moments&quot; + ago;

