* The multivariate normal probability density function
*
* @author Camille Weber <camille.weber@epfl.ch>
*/
public class MultNormalPdf {

public double[] mu;
/** Probability density function */
public double pdf(double[] x) {
if (x.length < mu.length)
throw new IllegalArgumentException(&quot;dimension of x is too small : &quot; + x.length + &quot; < &quot; + mu.length);

