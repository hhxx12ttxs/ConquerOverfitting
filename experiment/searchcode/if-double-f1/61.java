public FunctionSum(SmoothFunction f1,
SmoothFunction f2){
if(f1.getDomainDimension() != f2.getDomainDimension() ||
f1.getRangeDimension() != f2.getRangeDimension()){
this.f1 = f1;
this.f2 = f2;
}
@Override
public DoubleMatrix evaluate(DoubleMatrix x) {
// TODO Auto-generated method stub

