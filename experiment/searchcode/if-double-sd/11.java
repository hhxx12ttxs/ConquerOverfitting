double[] sd = spectrum.getData();
return getScore(sd, protein, shift);
}

protected double getScore(double[] sd, Protein protein, double shift) {
double[] pd = protein.getSpectrum();

