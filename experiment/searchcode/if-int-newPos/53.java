pos = 0;
}

public void setPos(int newpos) {
if(newpos < 0 || newpos >= len) {
throw new IndexOutOfBoundsException(&quot;Attempt to set new pos &quot; + newpos + &quot; is out of range 0 - &quot; + len);
public long getLong(char delimiter) {
int newpos = pos;
long value = 0;
boolean neg = false;

if(pos >= len) {
throw new RuntimeException(&quot;pos at end of string&quot;);

