c2 = this.tree.getCell(row - 1, cell    );	// Middle neighbour
c3 = this.tree.getCell(row - 1, cell + 1);	// Right neighbour

if(c1 &amp;&amp; c2 &amp;&amp; c3) {	// 111
return false;
}
if(c1 &amp;&amp; c2 &amp;&amp; !c3) {	// 110
return true;
}
if(c1 &amp;&amp; !c2 &amp;&amp; c3) {	// 101

