package Algebra;

public class FastFourierTransform {
private Complex[] fft(Complex[] x) {
int N = x.length;

if (N == 1)
return new Complex[] {x[0]};
Complex[] arr = new Complex[N >> 1];

