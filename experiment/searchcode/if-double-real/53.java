static final double EPS = 1e-15;

public double Real, Image;

//Constructors
public Complex(double R, double I) {
Real = R;
return (float) (Real * Real + Image * Image);
}

@Override
public double doubleValue() {
return (Real * Real + Image * Image);

