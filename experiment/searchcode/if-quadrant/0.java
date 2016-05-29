int result =0;
int quadrant;
if (x<width &amp;&amp; y<width){
quadrant=3;
} else if (x<width &amp;&amp; y>=width){
result=width*width;
} else if (x>=width &amp;&amp; y>=width){
quadrant=1;
x-=width;
y-=width;

