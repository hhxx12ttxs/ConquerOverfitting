boolean retVal = tmpNormInf != PrimitiveMath.ZERO;
ComplexNumber tmpVal;
double tmpNorm2 = PrimitiveMath.ZERO;

if (retVal) {
tmpNorm2 += tmpVal * tmpVal;
}
retVal = !TypeUtils.isZero(tmpNorm2);
}

if (retVal) {

double tmpScale = tmpVector[aRow] / tmpNormInf;

