for(int index = 0; index<this.size(); index++){
long thisMillis = this.get(index).getMillisSinceLastMessage();
if(thisMillis!= -1 &amp;&amp; cMillis>thisMillis) {
this.add(index + 1, c);

