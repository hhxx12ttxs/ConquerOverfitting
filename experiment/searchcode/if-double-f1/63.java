double x1 = a + (b - a) * rnd.nextDouble();
double x2 = a + (b - a) * rnd.nextDouble();
double x3 = a + (b - a) * rnd.nextDouble();

if(this.berechneDeterminante(x1, x2, x3) >= 10e-8){
return false;
}

private double berechneDeterminante(double x1, double x2, double x3) {
return f1.getFunktionswert(x1) * f2.getFunktionswert(x2) * f3.getFunktionswert(x3)

