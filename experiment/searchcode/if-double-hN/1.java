public RegulaFalsi(Horner hn, int genauigkeit, double anfangsnaeherung0, double anfangsnaeherung1, int maxIterationen){

if(Math.signum(hn.getFunktionswert(anfangsnaeherung0)) != (-1)*Math.signum(hn.getFunktionswert(anfangsnaeherung1))){

