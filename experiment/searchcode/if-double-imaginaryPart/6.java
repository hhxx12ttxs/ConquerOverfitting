public Double ImaginaryPart;
public ComplexNumber(Double real, Double imaginary){
RealPart=real; ImaginaryPart=imaginary;
//	this = new ComplexNumber(new Double(real), new Double(im));
//}
public void print(){
if (ImaginaryPart>=0)
System.out.println(&quot;&quot;+RealPart+&quot;+&quot;+ImaginaryPart+&quot;i&quot;);

