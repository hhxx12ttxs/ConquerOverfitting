package es.uab.pa.invaders.worldview;
import java.awt.*;

/**
 * 
 * Implementa un punto para el Mundo mediante sus coordenadas x e y.
 * 
 * La diferencia con respecto a la clase <code>java.awt.Point</code> es que usa coordendas <code>double</double>.
 * 
 * @author Vicenç soler
 *
 * 
 */
public class WPoint extends Object  
{
	/**
	 * Comment for <code>y</code> La Coordenada Y del WRectangle
	 */
	public double y;
	/**
	 * Comment for <code>x</code> La Coordenada X del WRectangle
	 */
	public double x;
	
/**
 * Constructor por defecto. No hace nada, ni inicializa variables.
 */
public WPoint()
{
}

/**
 * Construye un WPoint a partir de otro WPoint, generando una copia exacta.
 * @param p El WPoint a copiar.
 */
public WPoint(WPoint p)
{
	x = p.x;
	y = p.y;
}

/**
 * Construye un WPoint a partir de una coordenada (x,y).
 * @param x1 la coordenada X.
 * @param y1 la coordenada Y.
 */
public WPoint(double x1,double y1)
{
	x = x1;
	y = y1;
}

/**
 * Constructor a partir de un Point. Tiene en cuenta la constante World.DECIMALS para dividir cada coordenada por 10^DECIMALS.
 * @param p El Point
 */
public WPoint(Point p)
{
	x = p.x/Math.pow(10,World.DECIMALS);
	y = p.y/Math.pow(10,World.DECIMALS);
}

/**
 * Asigna los valores de una WPoint al WPoint actual.
 * @param p El WPoint del que copiará la x e y.
 */
public void assign(WPoint p)
{
	this.x = p.x;
	this.y = p.y;
}


/**
 * Compara el WPoint actual con otro WPoint para saber si contienen las mismas x e y.
 * @param p El WPoint a comparar.
 * @return true o false (si son iguales o no).
 */
public boolean compareTo(WPoint p)
{
	if (this.x == p.x && this.y == p.y) return true;
	return false;
}


/**
 * Retorna la distancia del WPoint actual a otro WPoint.
 * @param p El WPoint a calcular la distancia.
 * @return La distancia entre ambos puntos.
 */
public double distance(WPoint p)
{
	double d = Math.sqrt(Math.pow((p.x - this.x),2) + Math.pow((p.y - this.y),2));
	return d;
}

/**
 * Crea un nuevo WPoint a partir del actual, trasladándolo según el desplazamiento marcado en los parámetros.
 * No modifica el WPoint con el que se está trabajando.
 * @param x Es el desplazamiento X. No es la coordenada de destino, sino el desplazamiento X.
 * @param y Es el desplazamiento Y. No es la coordenada de destino, sino el desplazamiento Y.
 * @return El nuevo WPoint trasladado. 
 */
public WPoint virtualTraslate(double x,double y) {
	return new WPoint(this.x+x,this.y+y);
}

/**
 * Traslada el WPoint un desplazamiento (x,y1).
 * @param x Desplazamiento de las X.
 * @param y Desplazamiento de las Y.
 */
public void traslate(double x,double y) {
	this.x+=x;
	this.y+=y;
}

/**
 * Crea un nuevo WPoint a partir del actual, trasladándolo según el desplazamiento marcado en los parámetros.
 * El desplazamiento a aplicar es la diferencia entre los puntos ref_ini y ref_final que se pasan como parámetros.
 * El desplazamiento de las X es calculado así: ref_ini.x-ref_final.x , y el de las Y de la misma manera.
 * Se usa para aplicar el mismo desplazamiento a otros puntos, sabiendo el desplazamiento que se ha aplicado a un punto. Por ejemplo, si tene mos una figura con n puntos y queremos desplazarla, desplazamos uno de los puntos y el resto lo desplazaremos de la misma manera que hemos desplazado el primero.
 * No modifica el WPoint con el que se está trabajando.
 * @param ref_ini Punto inicial de referencia.
 * @param ref_final Punto final de referrencia.
 * @return El punt
 */
public WPoint traslateWithReference(WPoint ref_ini,WPoint ref_final) {
	double x1=ref_ini.x-ref_final.x,y1=ref_ini.y-ref_final.y;
	return new WPoint(x+x1,y+y1);
}


/**
 * Convierte a Point, simplemente multiplicando la coordenada por 10^numDecimals.
 * Usado para aprovechar funciones de Point.
 * @param numDecimals el número de decimales que se convierten en enteros.
 * @return el nuevo Point
 */
public Point toPoint(int numDecimals)
{
	return new Point((int)(x*Math.pow(10,numDecimals)),(int)(y*Math.pow(10,numDecimals)));
}


/* (non-Javadoc)
 * @see java.lang.Object#toString()
 */
public String toString()
{
	return new String("("+x+","+y+")");
}
	

/**
 * Retorna un nuevo WPoint correspondiente a la rotación del WPoint actual, un ángulo determinado y con respecto a un centro de rotación.
 * No se modifica el WPoint actual.  
 * @param angle El ángulo de rotación. Expresado en radianes.
 * @param center El WPoint correspondiente al centro de rotación.
 * @return El WPoint resultado de la rotación. 
 */
public WPoint rotate(double angle,WPoint center)
{
	WPoint wp=new WPoint(this);
	wp.x=center.x+(x-center.x)*Math.cos(angle)-(y-center.y)*Math.sin(angle);
	wp.y=center.y+(y-center.y)*Math.cos(angle)+(x-center.x)*Math.sin(angle);
	return wp;
}
}




