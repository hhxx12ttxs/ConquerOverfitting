* f(x,y) = c1*x + c2*y + c3
*
* @author liuyueming
*
*/
public class FXY extends AbstractFunction{
protected double c1,c2,c3=0.0;
varNames.add(Constant.y);
this.c1 = c1;
this.c2 = c2;
}

public FXY(double c1,double c2,double c3) {
varNames.add(Constant.x);

