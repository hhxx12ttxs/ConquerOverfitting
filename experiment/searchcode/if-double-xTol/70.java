public double getSolution(double[] x)
{
if (x==null || x.length != m_dimension)
throw new IllegalArgumentException(&quot;向量x为null或长度不等于给定维度&quot;);
double eps= 1.0e-7;
double xtol= 1.0e-16;
int icall=0;
int[] iflag = new int[1];
iflag[0]=0;

//init x[]
for (int i=0; i<n; i++)

