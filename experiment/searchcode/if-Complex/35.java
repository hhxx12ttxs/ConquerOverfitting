public final static double PI = 3.141592;
int n;
complex complex;
ArrayList<complex> complex_list;

FFT(ArrayList<complex> cl, complex c){
this.n = cl.size();
this.complex = c;
this.complex_list = cl;

// if the number of coefficients is not the power of 2, add 0s

