/** Mark beginning of statement.
*/
public void statBegin(int pos) {
//DEBUG.P(this,&quot;statBegin(int pos)&quot;);
//DEBUG.P(&quot;pos=&quot;+pos);

if (pos != Position.NOPOS) {
pendingStatPos = pos;

