int pathmap[][]=new int[200][3];
int exitloop=0;
int currentpos_i=0, currentpos_j=0,newpos_i=0, newpos_j=0;
newpos_j=currentpos_j+j;

if(newpos_i>=0 &amp;&amp; newpos_i<8 &amp;&amp; newpos_j>=0 &amp;&amp; newpos_j<8 &amp;&amp; visitmap[newpos_i][newpos_j]==0){

