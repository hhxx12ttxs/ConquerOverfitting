int gap2= this.data.elementAt(i).getElement() - this.data.elementAt(i-1).getElement();
ans= Math.min(gap1, gap2);
}
else if((i == 0) &amp;&amp; (i != this.data.size())){	//if the new link should be the first in this leaf
int gap1= this.data.elementAt(i+1).getElement() - this.data.elementAt(i).getElement();
int gap2;
if (this.data.elementAt(i).getPrev() != null)
gap2= this.data.elementAt(i).getElement() - (this.data.elementAt(i).getPrev()).getElement();

