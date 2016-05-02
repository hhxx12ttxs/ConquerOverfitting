package stuff;

import java.awt.Image;
import java.awt.geom.Rectangle2D;

public abstract class Rectangular extends PreImage{

	
	private int centroX, centroY, ancho, alto;

	public Rectangular() {
		super();
		centroX = centroY = ancho = alto = 0;
	}

	public Rectangular(Image icon) {
		super(icon);
		centroX = centroY = ancho = alto = 0;
	}

	public Rectangular(String name) {
		super(name);
		centroX = centroY = ancho = alto = 0;
	}

	public Rectangular(String name, int centroX, int centroY, int ancho, int alto) {
		super(name);
		this.centroX = centroX;
		this.centroY = centroY;
		this.ancho = ancho;
		this.alto = alto;
	}

	public int getCentroX() {
		return centroX;
	}

	public void setCentroX(int centroX) {
		this.centroX = centroX;
	}

	public int getCentroY() {
		return centroY;
	}

	public void setCentroY(int centroY) {
		this.centroY = centroY;
	}

	public int getAncho() {
		return ancho;
	}

	public void setAncho(int ancho) {
		this.ancho = ancho;
	}

	public int getAlto() {
		return alto;
	}

	public void setAlto(int alto) {
		this.alto = alto;
	}

	public int getMaxX(){
		return centroX + ancho/2;
	}
	
	public int getMaxY(){
		return centroY + alto/2;
	}
	
	public int getMinX(){
		return centroX - ancho/2;
	}
	
	public int getMinY(){
		return centroY - alto/2;
	}
	
	public boolean intersects(Circular c){
		return c.intersects(this);
	}

	public boolean intersects(Rectangular r){
//		for ( int i = getMinX(); i <= getMaxX(); i++)
//			for ( int j = getMinY(); j <= getMaxY(); j++)
//				if (r.contains(i, j)) return true;
//		return false;
	Rectangle2D.Double r1 = new Rectangle2D.Double(getMinX(), getMinY(), ancho, alto);
	Rectangle2D.Double r2 = new Rectangle2D.Double(r.getMinX(), r.getMinY(), r.ancho, r.alto);
	return r1.intersects(r2);
	}
	
	public boolean contains(int x, int y){
		return getMinX() <= x && x <= getMaxX() && getMinY() <= y && y <= getMaxY();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + alto;
		result = prime * result + ancho;
		result = prime * result + centroX;
		result = prime * result + centroY;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Rectangular other = (Rectangular) obj;
		if (alto != other.alto)
			return false;
		if (ancho != other.ancho)
			return false;
		if (centroX != other.centroX)
			return false;
		if (centroY != other.centroY)
			return false;
		return true;
	}
	
	
	
}

