retVal = !TypeUtils.isZero(tmpNorm2.doubleValue());
}

if (retVal) {

BigDecimal tmpScale = BigFunction.DIVIDE.invoke(tmpVector[aCol], tmpNormInf);
tmpNorm2 += tmpVal * tmpVal;
}
retVal = !TypeUtils.isZero(tmpNorm2);
}

if (retVal) {

double tmpScale = tmpVector[aCol] / tmpNormInf;

