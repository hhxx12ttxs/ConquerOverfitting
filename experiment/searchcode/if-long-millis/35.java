this.endMillis = System.currentTimeMillis();
}

public long getMillis () throws StopwatchException {
if(this.startMillis == -1 || this.endMillis == -1) throw new StopwatchException(&quot;Stopwatch has to be startet end stopped!&quot;);
this.startMillis = -1;
return res;
}

public long getSeconds () throws StopwatchException {
if(this.startMillis == -1 || this.endMillis == -1) throw new StopwatchException(&quot;Stopwatch has to be startet end stopped!&quot;);

