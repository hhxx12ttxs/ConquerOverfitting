Dimension size = new Dimension(getSize());
size.width--;
size.height--;
int xm = x + size.width/2;    // The &quot;middle&quot; of symbol in x direction
if(orientation.equals(&quot;UP&quot;) &amp;&amp; relationship==&#39;F&#39;)
drawUpAssociation(g,xm,xr,ym,yb);
else if(orientation.equals(&quot;DOWN&quot;) &amp;&amp; relationship==&#39;F&#39;)

