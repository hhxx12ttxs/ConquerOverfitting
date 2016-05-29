auxArray[i] = pI[i] - pJ[i];
}
double sumSq = StatUtils.sumSq(auxArray);

return (sumSq == 0.0 ? 0.0 : Math.sqrt(sumSq));
}

public static double perpendicularDistance(double[] pI, double[] pJ) {

