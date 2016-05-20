package figuras.elipse;

/**
 * @author Luis Treviďż˝o
 * 
 * Clase que representa una elipse mediante sus focos y la suma de la distancai de los
 * mismos a los puntos del perimetro.
 * Implementa el intefaz Figura.
 */


import figuras.FiguraAbstracta;
import figuras.Rectangulo;

import basicas.CompararDouble;
import basicas.Punto;


public class Elipse extends FiguraAbstracta{
	 
	private Punto f1;
	private Punto f2;
	private double cuerda;
	
	
	
	public Elipse() {
		super(0);
		f1 = new Punto(-1, 0);
		f2 = new Punto(1, 0);
		cuerda = 4;
	}
	
	public Elipse(Punto f1, Punto f2, double cuerda) {
		super(Punto.anguloTresPuntos(f1, f2, new Punto(f1.getX() + 1, f1.getY())));
		if ( f1 != null && f2 != null){ 
			this.f1 = f1;
			this.f2 = f2;
			if (cuerda < f1.distancia(f2))
				cuerda = f1.distancia(f2) + 1;
			if ( cuerda > 0)
				this.cuerda = cuerda;
			else
				throw new IllegalStateException("No pude ser negativa");
		}else{
			throw new NullPointerException("Los focos no pueden ser null.");
		}
	}
	
	public Elipse(Elipse e){
		super(e.getAngulo());
		f1 = new Punto(e.getOrigen());
		f2 = new Punto(e.getFoco2());
		cuerda = e.getCuerda();
	}
	
	public Punto getFoco1(){
		return f1;
	}
	
	public Punto getFoco2(){
		return f2;
	}
	
	public double getCuerda(){
		return cuerda;
	}
	
	@Override
	public double area(){
		return Math.PI * cuerda * figuraQueEnvuelve().getAlto() / 4;
	}

	@Override
	public Elipse mover(double dX, double dY) {
		f2.mover(dX, dY);
		f1.mover(dX, dY);
		return this;
	}
	
	@Override
	public Elipse mover(Punto p) {
		Punto c = getOrigen();
		double dX = p.getX() - c.getX();
		double dY = p.getY() - c.getY();
		f1.mover(dX, dY);
		f2.mover(dX, dY);
		return this;
	}
	
	@Override
	public Elipse escalar(double factor) {
		if (factor < 0) factor = 1 / (-factor);
		Punto p = getOrigen();
		f1 = new Punto((f1.getX()-p.getX())*factor + p.getX() , (f1.getY()-p.getY())*factor + p.getY());
		f2 = new Punto((f2.getX()-p.getX())*factor + p.getX() , (f2.getY()-p.getY())*factor + p.getY());
		cuerda *= factor;
		return this;
	}
	
	@Override
	public boolean estaContenido(Punto p) {
		if ( p == null) return false;
		else return (f1.distancia(p) + f2.distancia(p) <= cuerda); 
	}
	@Override
	public boolean estaEnPerimetro(Punto p) {
		if ( p == null) return false;
		double distancia = f1.distancia(p) + f2.distancia(p);
		return cuerda - tolerancia < distancia && cuerda + tolerancia > distancia ;
	}

	public Rectangulo rectanguloExterior(){
		double distancia = f1.distancia(f2);
		double alto = 2 * Math.sqrt(cuerda*cuerda/4 - distancia*distancia/4);
		return new Rectangulo(new Punto(getOrigen().getX()- cuerda/2, getOrigen().getY() - alto/2 ), cuerda, alto, super.angulo);
	}
	
	@Override
	public Rectangulo figuraQueEnvuelve() {
		return rectanguloExterior();
	}

	@Override
	public Punto getOrigen() {
		return new Punto((f2.getX()-f1.getX())/2 + f1.getX() , (f2.getY()-f1.getY())/2 + f1.getY());
	}
	
	@Override
	public Elipse rotar(double angulo) {
		double dis = f1.distancia(f2);
		super.angulo += angulo;
		if (CompararDouble.iguales(Math.abs(super.angulo / (2*Math.PI)), 1, 10)) super.angulo -= 2*Math.PI;
		if (Math.abs(super.angulo) > Math.PI * 2) super.angulo -= 2*Math.PI;
		Punto p = getOrigen();
		f1 = p.getPunto(dis/2, super.angulo + Math.PI);
		f2 = p.getPunto(dis/2, (super.angulo));
		return this;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(cuerda);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((f1 == null) ? 0 : f1.hashCode());
		result = prime * result + ((f2 == null) ? 0 : f2.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Elipse other = (Elipse) obj;
		if (! CompararDouble.iguales(cuerda, other.cuerda, 7))
			return false;
		if (f1 == null) {
			if (other.f1 != null)
				return false;
		} else if (!f1.equals(other.f1))
			return false;
		if (f2 == null) {
			if (other.f2 != null)
				return false;
		} else if (!f2.equals(other.f2))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "[f1=" + f1 + ", f2=" + f2 + ", cuerda=" + cuerda + "]";
	}	
}

