/*
 * Created on 25-oct-2006 by azabala
 *
 */
package com.iver.cit.gvsig.fmap.core.v02;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.print.attribute.PrintRequestAttributeSet;

import com.iver.cit.gvsig.fmap.core.FPolygon2D;
import com.iver.cit.gvsig.fmap.core.FPolyline2D;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.GeneralPathX;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.ISymbol;
import com.iver.utiles.StringUtilities;
import com.iver.utiles.XMLEntity;

/**
 * Symbol to draw a line with an arrow at the end.
 * @author alzabord
 */
public class FArrowSymbol implements ISymbol {

	protected static Color selectionColor = Color.YELLOW;
	
	protected static BufferedImage img = new BufferedImage(1, 1,
			BufferedImage.TYPE_INT_ARGB);
	
	protected static Rectangle rect = new Rectangle(0, 0, 1, 1);
	
	protected Color symColor;
	
	protected Stroke m_Stroke;
	
	protected int m_symbolType = FConstant.SYMBOL_TYPE_LINE;

	protected String m_Descrip;
	
	protected int rgb;

	protected boolean m_bDrawShape = true;
	
	protected int lenghtArrow = 15;
	protected int widthArrow = 10;
	
	
	
	
	public FArrowSymbol(Color symColor){
		this.symColor = symColor;
		calculateRgb();
	}
	
	public void calculateRgb() {
		// Recalculamos el RGB
		Graphics2D g2 = img.createGraphics();
		drawInsideRectangle(g2, g2.getTransform(), rect);
		rgb = img.getRGB(0, 0);
	}
	
	/**
	 * Devuelve el color que se aplica a los shapes seleccionados.
	 *
	 * @return DOCUMENT ME!
	 */
	public static Color getSelectionColor() {
		return selectionColor;
	}
	
	
	public Color getColor(){
		return symColor;
	}
	
	public Stroke getStroke(){
		return m_Stroke;
	}
	
	public void setStroke(Stroke stroke){
		this.m_Stroke = stroke;
	}
	
	
	/* (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.core.ISymbol#getSymbolForSelection()
	 */
	public ISymbol getSymbolForSelection() {
		FArrowSymbol solution = new FArrowSymbol(selectionColor);
		solution.m_bDrawShape = m_bDrawShape;
		solution.m_Stroke = m_Stroke;
		return solution;
	}
	
	
	

	
	public void draw(Graphics2D g, AffineTransform affineTransform, FShape shp) {
		
		//1? dibujamos la linea
		if (shp == null || (! isShapeVisible())) {
			return;
		}
        g.setColor(getColor());
		if (getStroke() != null) {
			g.setStroke(getStroke());
		}
		g.draw(shp);	
		
		/*
		 * Ahora intentamos obtener el ultimo segmento
		 * */
		PathIterator theIterator;
		theIterator = shp.getPathIterator(null, 0.8);
		int theType;
		double[] theData = new double[2];
		ArrayList arrayCoords = new ArrayList();
		while (!theIterator.isDone()) {
			theType = theIterator.currentSegment(theData);
		    if(theType == PathIterator.SEG_LINETO || theType == PathIterator.SEG_MOVETO)
		    	arrayCoords.add(theData);
		    theData = new double[2];
			theIterator.next();
		} //end while loop
		
		double length = 0d;
		double[] previous = null;
		for(int i = 0; i < arrayCoords.size(); i++ ){
			double[] coords = (double[]) arrayCoords.get(i);
			if(previous == null)
				previous = coords;
			else{
				double dx = coords[0] - previous[0];
				double dy = coords[1] - previous[1];
				double dist = Math.sqrt( ( dx * dx ) + (dy * dy));
				length += dist;
			}//else
		}//for
		
		if(lenghtArrow > (0.5 * length))//to avoid arrows collisions
			return;
		
		double[] last = (double[]) arrayCoords.get(arrayCoords.size() -1);
		double[] prevLast = (double[]) arrayCoords.get(arrayCoords.size() -2);
		
		
		double mx = last[0];
		double my = last[1];
		double Mx = prevLast[0];
		double My = prevLast[1];
//		

		// tama?o de la flecha
		double tipLength = lenghtArrow;
		double tipWidth = widthArrow;

		double	tip1x = mx + (((Mx - mx) * tipLength + ( tipWidth / 2)*(my - My))/
						Math.sqrt(( my - My) * (my - My)+(mx-Mx)*(mx-Mx)));
		
		double  tip2x = mx + (((Mx-mx) * tipLength-(tipWidth/2)*(my-My))/
						Math.sqrt((my-My)*(my-My)+(mx-Mx)*(mx-Mx)));
		
		double  tip1y = my + (((My-my)*tipLength-(tipWidth/2)*(mx-Mx))/
				 		Math.sqrt((my-My)*(my-My)+(mx-Mx)*(mx-Mx)));
		
		double tip2y = my + (((My-my)*tipLength+(tipWidth/2)*(mx-Mx))/
			            Math.sqrt((my-My)*(my-My)+(mx-Mx)*(mx-Mx)));
		
		GeneralPathX path = new GeneralPathX();
		path.moveTo(mx, my);
		path.lineTo(tip1x, tip1y);
		path.lineTo(tip2x, tip2y);
		path.closePath();
		FPolygon2D arrow = new FPolygon2D(path);
		g.fill(arrow);
	}

	
	public int getPixExtentPlus(Graphics2D g, AffineTransform affineTransform, Shape shp) {
		// TODO Auto-generated method stub
		return 0;
	}

	
	
	
	
	public int getOnePointRgb() {
		return rgb;
	}

	
	public XMLEntity getXMLEntity() {
		XMLEntity xml = new XMLEntity();
		xml.putProperty("className",this.getClass().getName());
		xml.putProperty("m_symbolType", getSymbolType());
		if (getColor() != null) {
			xml.putProperty("m_Color", StringUtilities.color2String(getColor()));
		}
		xml.putProperty("m_bDrawShape", isShapeVisible());
		
		
		//Ancho del stroke en float
		if (getStroke() != null) {
			xml.putProperty("m_stroke",
				((BasicStroke) getStroke()).getLineWidth());
		} else {
			xml.putProperty("m_stroke", 0f);
		}
		xml.putProperty("rgb", rgb);

		return xml;
	}

	
	
	public String getDescription() {
		return null;
	}

	
	
	
	
	public boolean isShapeVisible() {
		return m_bDrawShape;
	}
	
	
	
	public void setDescription(String m_Descrip) {
		this.m_Descrip = m_Descrip;
	}

	
	public int getSymbolType() {
		return m_symbolType;
	}
	
	

	public boolean isSuitableFor(IGeometry geom) {
		return true;
	}

	//TODO Copypasteado de graphics utilities para symboltype LINE
	public void drawInsideRectangle(Graphics2D g, 
			AffineTransform scaleInstance, 
			Rectangle r) {
		
		FShape shp;
		AffineTransform mT = new AffineTransform();
		mT.setToIdentity();

		Rectangle rect = mT.createTransformedShape(r).getBounds();
		GeneralPathX line = new GeneralPathX();
		
		
		
		line.moveTo(rect.x, rect.y + (rect.height / 2));
		line.curveTo(rect.x + (rect.width / 3),
			rect.y + (2 * rect.height),
			rect.x + ((2 * rect.width) / 3), rect.y - rect.height,
			rect.x + rect.width, rect.y + (rect.height / 2));

		shp = new FPolyline2D(line);
		draw(g, mT, shp);
	}

	
	

	
	public void setXMLEntity(XMLEntity xml) {
		
		
	}


	public String getClassName() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	/**
	 * @return Returns the m_bDrawShape.
	 */
	public boolean isM_bDrawShape() {
		return m_bDrawShape;
	}
	/**
	 * @param drawShape The m_bDrawShape to set.
	 */
	public void setM_bDrawShape(boolean drawShape) {
		m_bDrawShape = drawShape;
	}

	public void setPrintingProperties(PrintRequestAttributeSet printProperties) {
		// TODO Auto-generated method stub
		
	}
}

