Log.d(&quot;myLogs&quot;,&quot;now=&quot;+now+&quot; diff=&quot;+diff);
if (diff>60){
long  minuts=diff%60; //minuts
text=minuts+&quot; мин.&quot;;
}

if (diff>3600){
long   hours=diff/3600; //hours
text=hours+&quot; ч.&quot;;

