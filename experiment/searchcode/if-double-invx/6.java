DoubleMatrix1D M = null;

if(speedup){
DoubleMatrix2D X = QuQt(Q,u);

DoubleMatrix2D invX = null;
try{
invX = Algebra.DEFAULT.inverse(X);

