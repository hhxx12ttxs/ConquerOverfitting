double max = Math.max(Math.max(r, g) , b);
double min = Math.min(Math.min(r, g), b);

//V
this.v = max;

//S
this.s = (this.v - min)/this.v;

if(r== max &amp;&amp; g == min) {
this.h = 5 + (r - b)/(r- g);
} else if(r == max &amp;&amp; b == min) {

