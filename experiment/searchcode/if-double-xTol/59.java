public double apply(int n, Pointer<Double> x, Pointer<Double> gradient, Pointer<?> func_data) {
if (gradient != Pointer.NULL) {
double b = myFuncData.getDoubleAtIndex(1);
double t = a * x.getDoubleAtIndex(0) + b;
if (gradient != Pointer.NULL) {

