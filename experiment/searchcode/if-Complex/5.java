c.setComplex(((this.complex * other.real) - (this.real * other.complex)) / ((Math.pow(other.real, 2) + Math.pow(other.complex, 2))));
if(Double.isNaN(c.getReal()) || Double.isNaN(c.getComplex()))   {
this.complex = complex;
}

@Override
public String toString()    {
if(complex >= 0) {
return String.format(&quot;%.3f+%.3fi&quot;, real, complex);

