return 0.0D;
}
return covAB / Math.sqrt(varA * varB);
}

private double mean(List<Double> notas) {
if (notas.isEmpty()) {
double varX = 0.0D;
double varY = 0.0D;

if ((varX == 0.0D) || (varY == 0.0D)) {
return 0.0D;

