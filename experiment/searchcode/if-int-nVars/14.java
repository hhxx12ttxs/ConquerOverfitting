public void add(int ori, int dest) {
// TODO Auto-generated method stub
if(this.getOrigins(dest+nvars).size()<maxparents &amp;&amp; dest < nvars &amp;&amp; ori < nvars)
public boolean containsInter(int ori, int dest) {
if(dest < nvars &amp;&amp; ori < nvars)
return super.contains(ori, dest+nvars);

