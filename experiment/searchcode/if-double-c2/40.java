System.out.printf(&quot;%.2f %.2f\n&quot;,x,y);
}

void distance(Point c2)
{
double d;
d =  Math.sqrt(Math.pow((c2.get_x()-this.x),2.0)+Math.pow((c2.get_y()-this.y),2.0));
this.y = ((c1.get_y()+c2.get_y())/2.0);
}

void lineSlope(Point c2)
{
if((c2.get_x()-this.x)==0)
{
System.out.println(&quot;Undefined&quot;);

