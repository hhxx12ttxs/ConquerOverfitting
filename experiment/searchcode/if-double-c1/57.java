img = r * Math.sin(angleInRad);
type = &quot;polar&quot;;
}

public Complex add(Complex c1){
if (!this.type.equals(c1.type)){
}else{
double newReal = real + c1.real;
double newImg = img + c1.img;
return new Complex(newReal, newImg);

