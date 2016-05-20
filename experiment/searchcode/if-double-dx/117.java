package figuras.triangulo;


/**
 *@author Luis Treviďż˝o
 *
 * Clase que representa un triangulo mediante sus vertices.
 * Implementa el interfaz Figura
 */



import figuras.Circulo;
import figuras.FiguraAbstracta;
import figuras.elipse.Elipse;


import basicas.*;

public class Triangulo extends FiguraAbstracta{
	
	private Punto a;
	private Punto b;
	private Punto c;
	private double angA;

	
	public Triangulo(){
		super(0);
		a = new Punto(0,0);
		b = new Punto(0, 100);
		c = new Punto(40, 0);
		iniciarAngulo();
	}
		
	public Triangulo(Punto a, Punto b, Punto c) {
		super(0);
		if ( a == null || b == null || c == null){
			throw new NullPointerException("No puede haber un punto null.");
		}else{
			OrdenaTriangulo.ordenaVertices(this, a, b, c);
		}
		iniciarAngulo();
	}

	public Triangulo(Triangulo triangulo){
		super(0);
		if ( triangulo == null){
			throw new NullPointerException("El triangulo es null.");
		}else{
			a = new Punto(triangulo.a);
			b = new Punto(triangulo.b);
			c = new Punto(triangulo.c);
			super.angulo = triangulo.getAngulo();
		}
		iniciarAngulo();
	}
	
	private void iniciarAngulo(){
		Punto cen = Circuncentro.obtenerCircuncentro(this);
		angA = Punto.anguloTresPuntos(cen, new Punto(cen.getX()+10,cen.getY()), a);
		if(cen.getY() > a.getY()){
			if(cen.getX() <= a.getX()){
				angA += 3*Math.PI/2 ;
			}else{
				angA += Math.PI/2;
			}
		}
		if (CompararDouble.iguales(angA / (2*Math.PI), 1, 10) ||
				CompararDouble.comparar(angA, 2*Math.PI, 10) > 0) angA -= 2*Math.PI;
	}
	
	public Punto getA() {
		return a;
	}

	protected void setA(Punto a) {
		if (a == null) throw new NullPointerException();
		this.a = a;
	}

	public Punto getB() {
		return b;
	}

	protected void setB(Punto b) {
		if (b == null) throw new NullPointerException();
		this.b = b;
	}

	public Punto getC() {
		return c;
	}

	protected void setC(Punto c) {
		if (c == null) throw new NullPointerException();
		this.c = c;
	}
	
	@Override
	public double area(){
		return (a.distancia(c) * b.distancia(new Punto((a.getX() + c.getX()) / 2, (a.getY() + c.getY()) / 2))) / 2;
	}

	@Override
	public Triangulo mover(double dX, double dY) {
		a.mover(dX, dY);
		b.mover(dX, dY);
		c.mover(dX, dY);
		return this;
	}

	@Override
	public Triangulo escalar(double factor) {
		if (factor < 0) factor = 1 / (-factor);
		Punto cen = getOrigen();
		a.mover((a.getX() - cen.getX()) * factor - (a.getX() - cen.getX()), (a.getY() - cen.getY()) * factor - (a.getY() - cen.getY()));
		b.mover((b.getX() - cen.getX()) * factor - (b.getX() - cen.getX()), (b.getY() - cen.getY()) * factor - (b.getY() - cen.getY()));
		c.mover((c.getX() - cen.getX()) * factor - (c.getX() - cen.getX()), (c.getY() - cen.getY()) * factor - (c.getY() - cen.getY()));
		return this;
	}

	@Override
	public boolean estaContenido(Punto p) {
		if ( p == null ) return false;
		return estaContenido(a, b, c, p);
	}
	
	private boolean estaContenido(Punto p1, Punto p2, Punto p3, Punto p){
		if(VectorPunto.sentidoProductoVectorial(p1,p2,p3) >= 0)
			return VectorPunto.sentidoProductoVectorial(p1, p2, p) >= 0 &&
			VectorPunto.sentidoProductoVectorial(p2, p3, p) >= 0 &&
			VectorPunto.sentidoProductoVectorial(p3, p1, p) >= 0;
		else 
			return VectorPunto.sentidoProductoVectorial(p1, p2, p) <= 0 &&
					VectorPunto.sentidoProductoVectorial(p2, p3, p) <= 0 && 
					VectorPunto.sentidoProductoVectorial(p3, p1, p) <= 0;
	}

	@Override
	public boolean estaEnPerimetro(Punto p) {
		if ( p == null ) return false;
		Elipse e1 = new Elipse(a,b,tolerancia + a.distancia(b));
		Elipse e2 = new Elipse(b,c,tolerancia + b.distancia(c));
		Elipse e3 = new Elipse(a,c,tolerancia + a.distancia(c));
		return e1.estaContenido(p) || e2.estaContenido(p) || e3.estaContenido(p);
	}

//	@Override
//	public Triangulo rotar(double angulo) {
//		super.angulo += angulo;
//		if (CompararDouble.iguales(super.angulo / (2*Math.PI), 1, 10)) super.angulo -= 2*Math.PI;
//		b = a.getPunto(a.distancia(b), (super.angulo + Punto.anguloTresPuntos(a, b, c)));
//		c = a.getPunto(a.distancia(c), (super.angulo));
//		return this;
//	}
	
	@Override
	public Triangulo rotar(double angulo) {
		super.angulo += angulo;
		if (CompararDouble.iguales(super.angulo / (2*Math.PI), 1, 10) || CompararDouble.comparar(super.angulo, 2*Math.PI, 10) > 0) super.angulo -= 2*Math.PI;
		angA += angulo;
		if (CompararDouble.iguales(angA / (2*Math.PI), 1, 10) || CompararDouble.comparar(angA, 2*Math.PI, 10) > 0) angA -= 2*Math.PI;
		Punto cent = Circuncentro.obtenerCircuncentro(this);
		double dis = cent.distancia(a);
		if(! estaContenido(cent)){
			b = cent.getPunto(dis, Punto.anguloTresPuntos(cent, a, b) + angA);
			c = cent.getPunto(dis, Punto.anguloTresPuntos(cent, a , c) + angA);
		}else{
			b = cent.getPunto(dis, (2*Math.PI - Punto.anguloTresPuntos(cent, a, b)) + angA);
			c = cent.getPunto(dis, Punto.anguloTresPuntos(cent, a, c) + angA);
		}
		a = cent.getPunto(dis, angA);
		return this;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((a == null) ? 0 : a.hashCode());
		result = prime * result + ((b == null) ? 0 : b.hashCode());
		result = prime * result + ((c == null) ? 0 : c.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		return TrianguloEquals.comparaTriangulos(this, obj);
	}

	@Override
	public String toString() {
		return "[a=" + a + ", b=" + b + ", c=" + c + "]";
	}

	@Override
	public Triangulo mover(Punto p) {
		if (p == null) throw new NullPointerException();
		Punto cen = getOrigen();
		double dX = p.getX() - cen.getX();
		double dY = p.getY() - cen.getY();
		a.mover(dX, dY);
		b.mover(dX, dY);
		c.mover(dX, dY);
		return this;
	}

	@Override
	public Circulo figuraQueEnvuelve() {
		Punto cen = Circuncentro.obtenerCircuncentro(this);
		return new Circulo(cen, cen.distancia(b));
	}

	@Override
	public Punto getOrigen() {
		return Circuncentro.obtenerCircuncentro(this);
	} 
}

