this.den /= g;
}

private void normalize() {
if (this.den < 0) {
this.num *= -1;
this.den *= -1;
}
}

public int compareTo(Rational r) {

