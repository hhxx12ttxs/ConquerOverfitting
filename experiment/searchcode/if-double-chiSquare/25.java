double nRangeBound = (double) randomNumbers.length / rangeBound;
double chiSquare = 0;

for (int v : frequencyMap.values()) {
double f = v - nRangeBound;
chiSquare += f * f;
}

chiSquare /= nRangeBound;

