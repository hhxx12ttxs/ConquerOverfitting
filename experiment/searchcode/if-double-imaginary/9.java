private double mReal;
private double mImaginary;

public ComplexNumber(double real, double imaginary) {
mReal = real;
public ComplexNumber(ComplexNumber other) {
mReal = other.mReal;
mImaginary = other.mImaginary;
}

public double getReal() {

