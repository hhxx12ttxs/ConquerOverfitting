private static final double errTol = 0.00001;
double [][] mat = null;
int dimF = 0;
int dimC = 0;

public MatrizMath(int f, int c)
public MatrizMath sumar (MatrizMath obj) throws DistDimException
{
if(this.dimF != obj.dimF || this.dimC != obj.dimC)
throw new DistDimException(&quot; Distinta Dimension &quot;);

