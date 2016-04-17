package es.uab.pa.invaders.worldview;
import java.awt.*;

/**
 * 
 * Implementa un polígono para el Mundo mediante un vector de coordnadas X e Y
 * 
 * La diferencia con respecto a la clase <code>java.awt.Polygon</code> es que usa coordendas <code>double</double>.
 * 
 * @author Vicenç soler
 *
 * 
 */

public class WPolygon extends Object  
{
	/**
	 * Array de las X.
	 */
	public double x[];
	/**
	 * Array de las Y.
	 */
	public double y[];
	
	
	
/**
 * Constructor por defecto. No hace nada, ni crear los arrays x[] e y[].
 */
public WPolygon()
{
}

/**
 * Crea un WPolygon a partir de un Polygon. Usa World.DECIMALS para dividir cada coordenada por 10^DECIMALS.
 * @param p el Polygon 
 */
public WPolygon(Polygon p)
{
  x=new double[p.npoints];
  y=new double[p.npoints];
  
  for (int i=0;i<p.npoints;i++) {
  	x[i]=(double)p.xpoints[i]/Math.pow(10,World.DECIMALS);
  	y[i]=(double)p.ypoints[i]/Math.pow(10,World.DECIMALS);
  }
}

/**
 * Crea un WPolygon a partir de otro WPolygon, generando una copia exacta.
 * @param p el WPolygon
 */
public WPolygon(WPolygon p)
{
  x=new double[p.x.length];
  y=new double[p.x.length];
  
  for (int i=0;i<p.x.length;i++) {
  	x[i]=p.x[i];
  	y[i]=p.y[i];
  }
}

/**
 * Crea un WPolygon a través de una array de puntos x e y
 * @param x1 array de las x
 * @param y1 array de las y
 */
public WPolygon(double x1[], double y1[])
{
  x=new double[x1.length];
  y=new double[x1.length];
  
  for (int i=0;i<x1.length;i++) {
  	x[i]=x1[i];
  	y[i]=y1[i];
  }
}


/**Crea un WPolygon a través de 4 WPoint. Tienen que ser 4 puntos pintados seguidos.
 * @param p1
 * @param p2
 * @param p3
 * @param p4
 */
public WPolygon(WPoint p1,WPoint p2,WPoint p3,WPoint p4)
{
  x=new double[4];
  y=new double[4];
  
  x[0]=p1.x;
  y[0]=p1.y;
  x[1]=p2.x;
  y[1]=p2.y;
  x[2]=p3.x;
  y[2]=p3.y;
  x[3]=p4.x;
  y[3]=p4.y;
  
}

/**
 * Retorna uno de los WPoints de los que forma el WPolygon
 * @param ind El numero de índice del punto, comenxzando por 0
 * @return El WPoint
 */
public WPoint getPoint(int ind) {
	return new WPoint(x[ind],y[ind]);
}





/**
 * Crea un Polygon a partir de este WPolygon. Se crea para usar funciones de Polygon que no están en WPolygon 
 * @param numDecimals Se multiplican las coordenadas por 10^numDecimals
 * @return El polygon creado
 */
public Polygon toPolygon(int numDecimals)
{
	int []x1=new int[x.length];
  int []y1=new int[x.length];
  for (int i=0;i<x.length;i++) {
  	x1[i]=(int)(x[i]*Math.pow(10,numDecimals));
  	y1[i]=(int)(y[i]*Math.pow(10,numDecimals));
  }
	return new Polygon(x1,y1,x1.length);
}

/**
 * Retorna true si el WPolygon intersecciona con el WPolygon pasado como parámetro.
 * @param r Es el WPolygon con el que se quiere verificar la intersección.
 * @return true o false (si intersecciona o no).
 */
public boolean intersects(WPolygon r) {
//	retornem el rectangle que conté el poligon
	//return toPolygon(World.DECIMALS).intersects(r.toPolygon(World.DECIMALS).getBounds2D());
	for(int i=0;i<r.x.length;i++) 
	  if (contains(r.getPoint(i))) return true;
	for(int i=0;i<x.length;i++) 
	  if (r.contains(getPoint(i))) return true;
	return false;
}

/**
 * Examina si el WPolygon contiene un WPoint.
 * @param p El WPoint que se quiere examinar.
 * @return trueo false (si lo contiene o no)
 */
public boolean contains(WPoint p) {
	return toPolygon(World.DECIMALS).contains(p.toPoint(World.DECIMALS));
}

/**
 * Crea un nuevo WPolygon a partir del actual, trasladándolo según el desplazamiento marcado en los parámetros.
 * No modifica el WPolygon con el que se está trabajando.
 * @param x1 Es el desplazamiento X. No es la coordenada de destino, sino el desplazamiento que se aplica a cada punto del WPolygon
 * @param y1 Es el desplazamiento Y. No es la coordenada de destino, sino el desplazamiento que se aplica a cada punto del WPolygon
 * @return El nuevo WPolygon trasladado. 
 */
public WPolygon virtualTraslate(double x1,double y1)
{
	WPolygon p=new WPolygon(this);
	for (int i=0;i<p.x.length;i++) {
  	p.x[i]+=x1;
  	p.y[i]+=y1;
  }
  return p;
}
/**
 * Crea un nuevo WPolygon a partir del actual, trasladandolo según el desplazamiento marcado en el parámetro.
 * No modifica el WPolygon con el que se está trabajando.
 * @param p Es el desplazamiento marcado en el WPoint que se pasa. No es la coordenada de destino, sino el desplazamiento que se aplica a cada punto del WPolygon, tanto para las X como para las Y.
 * @return El nuevo WPolygon trasladado.
 */
public WPolygon virtualTraslate(WPoint p)
{
	return virtualTraslate(p.x,p.y);
}

/**
 * Traslada todos los puntos del WPolygon un desplazamiento (x1,y1).
 * @param x1 Desplazamiento de las X.
 * @param y1 Desplazamiento de las Y.
 */
public void traslate(double x1,double y1) {
	for (int i=0;i<x.length;i++) {
  	x[i]+=x1;
  	y[i]+=y1;
  }
}

/**
 * Retorna un nuevo WPolygon correspondiente a la rotación del WPolygon actual, un ángulo determinado y con respecto a un centro de rotación.
 * No se modifica el WPolygon actual.  
 * @param angle El ángulo de rotación. Expresado en radianes.
 * @param centre El WPoint correspondiente al centro de rotación.
 * @return El nuevo WPolygon resultado de la rotación. 
 */
public WPolygon rotate(double angle,WPoint centre) {
	WPolygon pol=new WPolygon(this);
	WPoint p;
	for (int i=0;i<x.length;i++) {
		p=(new WPoint(pol.x[i],pol.y[i])).rotate(angle,centre);
  	    pol.x[i]=p.x;
  	    pol.y[i]=p.y;
    }
	return pol;
}

/**
 * Transformada del WPolygon, de Mundo a Pantalla.
 * @param w El objeto World al que se aplica la transformada.
 * @return El Polygon resultado de aplicar la transformada.
 */
public Polygon worldToView(World w)
{
	Polygon pol=toPolygon(0);
	Point p;
	WPoint wp=new WPoint();
	for (int i=0;i<pol.npoints;i++) {
		wp.x=x[i];wp.y=y[i];
		p=w.worldToView(wp);
		pol.xpoints[i]=p.x;
  	pol.ypoints[i]=p.y;
  }
	return pol;
}

/**
 * Transformada de la Pantalla al Mundo.
 * @param w El objeto World al que se aplica la transformada. 
 * @param pol1 El Polygon a transformar.
 * @return El WPolygon resultado de aplicar la transformada.
 */
public static WPolygon viewToWorld(World w,Polygon pol1)
{
	WPolygon pol=new WPolygon(pol1);
	Point p=new Point(0,0);
	WPoint wp;
	for (int i=0;i<pol.x.length;i++) {
		p.x=pol1.xpoints[i];p.y=pol1.ypoints[i];
		wp=w.viewToWorld(p);
  	pol.x[i]=wp.x;
  	pol.y[i]=wp.y;
  }
	return pol;
}	

/* (non-Javadoc)
 * @see java.lang.Object#toString()
 */
public String toString()
{
	String s="";
	for (int i=0;i<x.length;i++) {
		s+="("+x[i]+","+y[i]+") ";
	}
	return s;
}
	
}
