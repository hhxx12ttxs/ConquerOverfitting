ComplexNumber c = new ComplexNumber(this.getA() * n2.getA() - this.getBi() * n2.getBi(), this.getA() * n2.getBi() + this.getBi() * n2.getA());
return c;
}

public void mult2(ComplexNumber n2) {
double x = this.a;
this.bi = x * n2.bi + this.bi * n2.a;
}

public ComplexNumber div(ComplexNumber n2) {
double z = n2.getA() * n2.getA() + n2.getBi() * n2.getBi();

