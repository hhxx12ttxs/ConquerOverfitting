System.out.println(&quot;Dividing &quot; + p + &quot; and &quot; + p.getNext());
p = p.getNext();
quotient /= p.computePolyNode(x);
if (p.computePolyNode(x) == 0)
return new PolyNode(0,0,null);
if (p.getDegree() == 0 &amp;&amp; x == 0)

