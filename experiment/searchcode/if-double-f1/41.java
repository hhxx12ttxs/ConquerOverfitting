* Created by gijoe on 6/21/2015.
*/
public class Scale implements Function {

private Function f1;
private double k1;
private double k2;

public Scale(Function f1, double k1, double k2) {
this.f1 = f1;
this.k1 = k1;

