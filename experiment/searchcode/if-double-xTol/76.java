* @return 求解结束最后一轮的函数值
*/
public double getSolution(double[] x)
{
if (x==null || x.length != m_dimension)
boolean diagco= false;
double eps= 1.0e-7;
double xtol= 1.0e-16;
int icall=0;
int[] iflag = new int[1];
iflag[0]=0;

