public Matrix[] LUDecompose(Matrix A) {
// TODO Auto-generated method stub
if(!A.isSquare()){
return null;
}
int n = A.getNumRows();
for(i=0; i<=j; i++){
double val = 0.0;
for(int k=0; k<i; k++){
val += L.getElem(i, k)*U.getElem(k, j);

