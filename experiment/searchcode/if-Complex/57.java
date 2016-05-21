    int nx2 = _sx2.getCount();
 * Arrays input to forward transforms may contain either real or
    if (_complex!=complex) {
 * (real,imaginary) pairs of consecutive floats. The default input 
      _fft1c.complexToComplex1(_sign1,nx2,fpad,fpad);
    if (_complex) {
    float[] fpad = pad(f);
    if (_complex) {
      _fft1c.complexToComplex(_sign1,fpad,fpad);
  public void setComplex(boolean complex) {
    if (_complex) {
    int nx3 = _sx3.getCount();
 * complex values. If complex, values are packed sequentially as 
      _fft1c.complexToComplex1(_sign1,nx2,nx3,fpad,fpad);
      _complex = complex;

