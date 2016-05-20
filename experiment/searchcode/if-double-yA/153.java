/* gvSIG. Sistema de Informaci?n Geogr?fica de la Generalitat Valenciana
 *
 * Copyright (C) 2005 IVER T.I. and Generalitat Valenciana.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
 *
 * For more information, contact:
 *
 *  Generalitat Valenciana
 *   Conselleria d'Infraestructures i Transport
 *   Av. Blasco Ib??ez, 50
 *   46010 VALENCIA
 *   SPAIN
 *
 *      +34 963862235
 *   gvsig@gva.es
 *      www.gvsig.gva.es
 *
 *    or
 *
 *   IVER T.I. S.A
 *   Salamanca 50
 *   46005 Valencia
 *   Spain
 *
 *   +34 963163400
 *   dac@iver.es
 */
package com.iver.cit.gvsig.fmap.core.symbols;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.PrintQuality;

import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.ViewPort;
import com.iver.cit.gvsig.fmap.core.CartographicSupportToolkit;
import com.iver.cit.gvsig.fmap.core.FPoint2D;
import com.iver.cit.gvsig.fmap.core.FPolygon2D;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.GeneralPathX;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.core.v02.FConverter;
import com.iver.utiles.StringUtilities;
import com.iver.utiles.XMLEntity;
import com.iver.utiles.swing.threads.Cancellable;

/**
 * SimpleTextSymbol is a class used to create symbols composed using a text defined by
 * the user.This text can be edited (changing the color, the font of the characters, and
 * the rotation of the text).
 * @author jaume dominguez faus - jaume.dominguez@iver.es
 *
 */
public class SimpleTextSymbol extends AbstractSymbol implements ITextSymbol {
	private String text = "";
	private Font font = SymbologyFactory.DefaultTextFont;
	private Color textColor = Color.BLACK;
	private double rotation;
	private FontRenderContext frc = new FontRenderContext(
			new AffineTransform(), false, true);
	private boolean autoresize;
	private Rectangle bounds = null;
	private FShape horizontalTextWrappingShape = null;
	private PrintRequestAttributeSet properties;

	public void draw(Graphics2D g, AffineTransform affineTransform, FShape shp, Cancellable cancel) {
		if (!isShapeVisible()) return;
		double shpX = ((FPoint2D) shp).getX();
		double shpY = ((FPoint2D) shp).getY();
		//Parche porque a veces llegan puntos cuyas coordenadas no han podido ser calculadas y vienen como NaN
		if( Double.isNaN(shpX) || Double.isNaN(shpY)){
			return;
		}
		//Fin del parche
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(textColor);
		g.setFont(font);
		g.translate(shpX, shpY);

		g.rotate(rotation);
		Rectangle2D bounds = getHorizontalTextWrappingShape(new FPoint2D(0,0)).getBounds();
		// Antes tomabamos el resultado de getBounds pero ya se le hab?a aplicado
		// la rotaci?n, con lo que no obten?amos el la altura correcta de la fuente.

		// Alineamos el texto de manera que la parte inferior
		// izquierda de la primera letra est? en (0,0).
		// Para chino hay que escoger una fuente como esta (SimSun)
//		g.setFont(new Font("SimSun",Font.PLAIN, 12));

		g.drawString(getText(), -((int) bounds.getWidth()/2), 0); //(int)-bounds.getY());
//		g.drawRect(0, 0, 5, 5);
		g.rotate(-rotation);
		g.translate(-shpX, -shpY);
	}

	public void drawInsideRectangle(Graphics2D g,
			AffineTransform scaleInstance, Rectangle r, PrintRequestAttributeSet properties) throws SymbolDrawingException {
		int s = getFont().getSize();

		if (autoresize) {
			if (s==0) {
				s =1;
				setFontSize(s);
			}
			g.setFont(getFont());
		    FontMetrics fm = g.getFontMetrics();
		    Rectangle2D rect = fm.getStringBounds(text, g);
		    double width = rect.getWidth();
		    double height = rect.getHeight();
		    double rWidth = r.getWidth();
		    double rHeight = r.getHeight();
		    double ratioText = width/height;
		    double ratioRect = rWidth/rHeight;

		    if (ratioText>ratioRect) {
		    	s = (int) (s*(rWidth/width));
		    } else {
		    	s = (int) (s*(rHeight/height));
		    }
		    setFontSize(s);
		}

		//Only for debugging purpose
//		g.drawRect((int)r.getX(), (int)r.getY(), (int)r.getWidth(), (int)r.getHeight());
		if (properties==null)
			draw(g, null, new FPoint2D(r.getX(), r.getY()), null);
		else
			print(g, new AffineTransform(), new FPoint2D(r.getX(), r.getY()), properties);

	}

	public int getOnePointRgb() {
		return textColor.getRGB();
	}

	public void getPixExtentPlus(FShape shp, float[] distances,
			ViewPort viewPort, int dpi) {
		throw new Error("Not yet implemented!");

	}

	public ISymbol getSymbolForSelection() {
		return this; // a text is not selectable
	}

	public int getSymbolType() {
		return FShape.TEXT;
	}

	public XMLEntity getXMLEntity() {
		XMLEntity xml = new XMLEntity();
		xml.putProperty("className", getClassName());
		xml.putProperty("desc", getDescription());
		xml.putProperty("isShapeVisible", isShapeVisible());
		xml.putProperty("font", font.getName());
		xml.putProperty("fontStyle", font.getStyle());
		xml.putProperty("size", font.getSize());
		xml.putProperty("text", text);
		xml.putProperty("textColor", StringUtilities.color2String(textColor));
		xml.putProperty("unit", getUnit());
		xml.putProperty("referenceSystem", getReferenceSystem());
		xml.putProperty("autoresizeFlag", isAutoresizeEnabled());
		return xml;
	}

	public boolean isSuitableFor(IGeometry geom) {
		return true;
	}

	public void setXMLEntity(XMLEntity xml) {
		font = new Font(xml.getStringProperty("font"),
				xml.getIntProperty("fontStyle"),
				xml.getIntProperty("size"));
		setDescription(xml.getStringProperty("desc"));
		setIsShapeVisible(xml.getBooleanProperty("isShapeVisible"));
		text = xml.getStringProperty("text");
		textColor = StringUtilities.string2Color(xml.getStringProperty("textColor"));
		setUnit(xml.getIntProperty("unit"));
		setReferenceSystem(xml.getIntProperty("referenceSystem"));
		setAutoresizeEnabled(xml.getBooleanProperty("autoresizeFlag"));
		this.bounds = null;
		this.horizontalTextWrappingShape = null;
	}

	public String getClassName() {
		return getClass().getName();
	}

	public void print(Graphics2D g, AffineTransform at, FShape shape, PrintRequestAttributeSet properties) {
		this.properties=properties;
        draw(g, at, shape, null);
        this.properties=null;

	}

	public String getText() {
		return text;
	}

	public Font getFont() {
		return font;
	}

	public Color getTextColor() {
		return textColor;
	}

	public void setText(String text) {
		if(text != null && !text.equals(this.text)){
			this.text = text;
			this.bounds = null;
			this.horizontalTextWrappingShape = null;
		}
	}

	public void setFont(Font font) {
		if (font != null && !font.equals(this.font)){
			this.font = font;
			this.bounds = null;
			this.horizontalTextWrappingShape = null;
		}
	}

	public void setTextColor(Color color) {
		this.textColor = color;
	}

	public void setFontSize(double size) {
		if (size != this.font.getSize2D()){
			this.font = this.font.deriveFont((float) size);
			this.bounds = null;
			this.horizontalTextWrappingShape = null;
		}
//		this.font = new Font(this.font.getName(),this.font.getStyle(),(int)size);
	}

	/**
	 * Defines the angle of rotation for the text that composes the symbol
	 *
	 * @param rotation
	 */
	public void setRotation(double rotation) {
		if(rotation != this.rotation){
			this.rotation = rotation;
			this.bounds = null;
		}
	}

	public double getRotation() {
		return rotation;
	}

	/**
	 * Returns an FShape which represents a rectangle containing the text in
	 * <b>screen</b> units.
	 */
	private FShape getHorizontalTextWrappingShape(FPoint2D p) {
		if (this.horizontalTextWrappingShape  == null){
			Font font = getFont();
			/* Para tama?os de fuente de letras excesivamente grandes obtenemos
			 * shapes con todas las coordenadas a 0, por eso limitamos el tama?o
			 * a 1000 y despu?s reescalamos el bounds.
			 */
			double scale = 1;
			float fontSize = font.getSize2D();
			if (fontSize > 1000){
				scale = fontSize/1000;
				fontSize = 1000;
			}
			font = font.deriveFont(fontSize);
			GlyphVector gv = font.createGlyphVector(frc, text);
			Shape shape = gv.getOutline((float) p.getX(), (float) p.getY());
			FShape myFShape = new FPolygon2D(new GeneralPathX(shape.getBounds2D()));

			if(scale != 1){
				myFShape.transform(AffineTransform.getScaleInstance(scale, scale));
			}

			this.horizontalTextWrappingShape = myFShape;
		}
		return this.horizontalTextWrappingShape;
	}

	/**
	 * Returns an FShape which represents a rectangle containing the text in
	 * <b>screen</b> units.
	 */
	public FShape getTextWrappingShape(FPoint2D p) {

		FShape myFShape = getHorizontalTextWrappingShape(p);
		myFShape.transform(AffineTransform.getTranslateInstance(p.getX(), p.getY()));

		if (rotation != 0) {
			myFShape.transform(AffineTransform.getRotateInstance(rotation));
		}
		return myFShape;
	}

	public Rectangle getBounds() {
//		FontMetrics fm = g.getFontMetrics();
//		Rectangle2D rect = fm.getStringBounds("graphics", g);

		if(this.bounds == null){
			this.bounds = getTextWrappingShape(new FPoint2D(0,0)).getBounds();
		}
		return this.bounds;
	}

	public void setCartographicSize(double cartographicSize, FShape shp) {
		setFontSize(cartographicSize);
	}

	public double toCartographicSize(ViewPort viewPort, double dpi, FShape shp) {
		double oldSize = getFont().getSize();
		setCartographicSize(getCartographicSize(
								viewPort,
								dpi,
								shp),
							shp);
		return oldSize;
	}

	public double getCartographicSize(ViewPort viewPort, double dpi, FShape shp) {
		return CartographicSupportToolkit.
					getCartographicLength(this,
										  getFont().getSize(),
										  viewPort,
										  dpi);
	}

	public boolean isAutoresizeEnabled() {
		return autoresize;
	}

	public void setAutoresizeEnabled(boolean autoresizeFlag) {
		this.autoresize = autoresizeFlag;
	}
}

