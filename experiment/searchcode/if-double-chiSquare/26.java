private final int M;

public ChiSquare(int M) {
this.M = M;
}

public boolean isDependent(double pxi, double pxj, double pxixj) {
if ((pxi == 0) || (pxj == 0)) {
return false;
}

final double chiSquare = (M * (pxixj - (pxi * pxj)) * (pxixj - (pxi * pxj)))

