public class Complex extends Number
{
double imaginary;

public Complex ( double re
Number add ( Number operand)
{
if (operand instanceof Complex)
{ Complex cmplx = (Complex) operand;
return new Complex( real + cmplx.real, imaginary + cmplx.imaginary);

