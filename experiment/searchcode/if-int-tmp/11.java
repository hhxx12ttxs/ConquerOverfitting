String tmp2 = &quot;&quot;;
int H,W;
int tmpI, tmpJ;
int tmpI2, tmpJ2;
int cases = 1;
Queue<Integer> myQ = new LinkedList<Integer>();
while(! tmp.equals(&quot;0 0&quot;))
{
H = Integer.parseInt(tmp.substring(0, tmp.indexOf(&quot; &quot;)));
W = Integer.parseInt(tmp.substring(tmp.indexOf(&quot; &quot;)+1))*4;

