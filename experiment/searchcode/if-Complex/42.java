if (inverse) {
dctTransform.inverse(dctData, false);
// array contains [real1,complex1,real2,complex2,...,realn,complexn]
// array gets padded with zero if datasize<fftsize
// if complex = false
for (int i = 0; i < fftSize; i++) {
fftTransform.complexForward(fftData);
int mul = 1; // multiplier for complex numbers
int add = 0; // shift for complex numbers
if (complex) {
mul = 2;

