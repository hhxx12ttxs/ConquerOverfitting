* @throws JMException
*/
public double b_poly(double y, double alpha) throws Exception{
if (!(alpha>0)) {
* b_flat transformation
*/
public double b_flat(double y, double A, double B, double C){
double tmp1 = Math.min((double)0, (double)Math.floor(y - B))* A*(B-y)/B;

