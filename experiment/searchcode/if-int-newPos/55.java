Log.i(TAG, &quot;FastParser - &quot;+delimiter);

}

public void setLine(char[] line, int len) {
if(MyLog.enabled) {
this.line = line;
this.len = len;
pos = 0;
}

public void setPos(int newpos) {
if(newpos < 0 || newpos >= len) {

