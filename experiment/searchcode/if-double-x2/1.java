x2 = 0 - b + Math.sqrt(d); x2 = x2 / a;	x2 = x2 / 2;
if( x1 > x2) { double tmp = x1; x1 = x2; x2 = tmp;}

}

public boolean hasSolutions()
else
{
return(0);
}
}

public double getSolution2()
{
if(solutions)
{
return(x2);
}
else
{
return(0);
}
}
}

