public static String getString(int Seconds){
String output=&quot;&quot;;
if(Seconds/3600>=1){
// There&#39;s hours.
int Hours = (int) Math.floor(Seconds/3600);
// There&#39;s minutes.
int Minutes = (int) Math.floor(Seconds/60);
if(Minutes>1){
output = output+&quot; &quot;+Minutes+&quot; minutes&quot;;

