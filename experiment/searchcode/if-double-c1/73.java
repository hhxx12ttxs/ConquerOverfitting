* f(x,y) = c1*x + c2*y + c3
*
* @author liuyueming
*
*/
public class FXY extends AbstractFunction{
protected double c1,c2,c3=0.0;

public FXY(double c1,double c2) {
varNames.add(Constant.x);
varNames.add(Constant.y);

