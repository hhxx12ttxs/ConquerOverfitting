public Pair<Double> find(double[] darray) {
Arrays.sort(darray);
double minDiff = Double.MAX_VALUE;
Pair<Double> resultPair = null;
double result = Math.abs(darray[i] - darray[i+1]);
if (result < minDiff) {
resultPair = new Pair<Double>(darray[i], darray[i+1]);
minDiff = result;
}
}

return resultPair;
}
}

