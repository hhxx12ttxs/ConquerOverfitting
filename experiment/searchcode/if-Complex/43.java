public static Complex[] fftDit1d(Complex[] complex){
final int N = complex.length;

// base case
if (N == 1){
return new Complex[] { complex[0] };
}

// radix 2 Cooley-Tukey FFT
if (N % 2 != 0) {

