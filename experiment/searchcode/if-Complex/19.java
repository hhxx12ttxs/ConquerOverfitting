public Complex exp()                {return new Complex(Math.pow(E,this.x)*Math.cos(this.y),Math.pow(E,this.x)*Math.sin(this.y));}

public Complex pow(int n)
{
double[] p = toPolar(this);
if      (n==0)                  return new Complex(1,0);
else if (n>0)                   return new Complex(Math.pow(p[0],n)*Math.cos(n*Math.cos(p[1])),Math.pow(p[0],n)*Math.sin(n*Math.sin(p[1])));

