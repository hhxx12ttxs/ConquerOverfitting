public Collection<T> testArea(double XA, double YA, double XB, double YB){
if(!Finalized){
throw new IllegalArgumentException(&quot;Attempt to read before Hash is Finalized&quot;);
if(XA > XB){double T = XB; XB = XA; XA = T;}
if(YA > YB){double T = YB; YB = YA; YA = T;}

