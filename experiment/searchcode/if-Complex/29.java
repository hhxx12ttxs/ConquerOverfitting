// E&#39; usada para implementarmos polinomios

public class Poly implements HolomorphicFunction  {

private int d;
private Complex[] r;

public Poly(Complex[] r) {
this.d = r.length;
this.r = r;
}
//retorna f(x) em x
public Complex eval(Complex x) {

