public NaturalLog(Function a)
{
f1 = a.simplify();
}

public double eval(double x)
{
return (Math.log(f1.eval(x)));
}

public Number eval(Number n)
{
Number n1 = f1.eval(n);

if (n1 instanceof Infinity || n1 instanceof NegInfinity)

