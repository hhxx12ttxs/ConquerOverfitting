public Pixel lighten(double factor){
if(factor>=0.0 &amp;&amp; factor<=1.0){
red = red * (1.0 - factor) + factor;
blue = blue * (1.0 - factor) + factor;
return new ColorPixel(red, green, blue);
}

public Pixel darken(double factor){
if(factor>=0.0 &amp;&amp; factor<=1.0){
red = red * (1.0 - factor);

