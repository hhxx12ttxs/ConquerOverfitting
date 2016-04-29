public class Complex extends Number
{
  double imaginary;

  public Complex ( double re
                 , double im)
  {
    super( re);
    imaginary = im;
  }

  Number add ( Number operand)
  {
    if (operand instanceof Complex)
    { Complex cmplx = (Complex) operand;
      return new Complex( real + cmplx.real, imaginary + cmplx.imaginary);
    }
    else
    { return new Complex( real + operand.real, imaginary);
    }
  }

  Number multiply ( Number operand)
  {
    if (operand instanceof Complex)
    { Complex cmplx = (Complex) operand;
      return
        new Complex
          ( real * cmplx.real - imaginary * cmplx.imaginary
          , real * cmplx.imaginary + imaginary * cmplx.real);
    }
    else
    { return new Complex( real * operand.real, imaginary * operand.real);
    }
  }
}

