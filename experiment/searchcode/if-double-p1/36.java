public double dotProduct(double[] A, double[] B, double[] C) {

double[] P1 = getVector(A, B);
double[] P2 = getVector(A, C);

return (P1[0]*P2[0] + P1[1]*P2[1]);
}

//return ABxAC
public double crossProduct(double[] A, double[] B, double[] C) {

