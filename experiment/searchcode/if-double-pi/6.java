if (t==0) return b;  if ((t/=d)==1) return b+c;
if (a < Math.abs(c)) { a=c;  s=p/4; }
else { s = p/(2*(double)Math.PI) * (double)Math.asin (c/a);}
if (a < Math.abs(c)) { a=c; s=p/4; }
else { s = p/(2*(double)Math.PI) * (double)Math.asin (c/a);}
if (t < 1) return -.5f*(a*(double)Math.pow(2,10*(t-=1)) * (double)Math.sin( (t*d-s)*(2*(double)Math.PI)/p )) + b;

