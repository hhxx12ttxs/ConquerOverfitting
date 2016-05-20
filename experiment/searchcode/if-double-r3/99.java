/*
 * Created on 28-abr-2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
/* gvSIG. Sistema de Informaci?n Geogr?fica de la Generalitat Valenciana
 *
 * Copyright (C) 2004 IVER T.I. and Generalitat Valenciana.
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
package com.iver.cit.gvsig.fmap.core.v02;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.apache.batik.ext.awt.geom.PathLength;

import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.core.FPoint2D;
import com.iver.cit.gvsig.fmap.core.FPolygon2D;
import com.iver.cit.gvsig.fmap.core.FPolyline2D;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.GeneralPathX;
import com.iver.cit.gvsig.fmap.core.Handler;
import com.iver.cit.gvsig.fmap.core.symbols.IFillSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.ILineSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.ISymbol;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;


/**
 * Clase con m?todos est?ticos para dibujar sobre el Graphics que se les pase
 * como par?metro.
 *
 * Esta clase deber?a ser privada. Las clases que la usan son GraphicLayer
 * y AnnotationStrategy, pero hay que revisarlas para que no sea necesario.
 * Lo m?s urgente ser?a lo del dibujado de textos, para que sea
 * Hay que quitar las dependecias de FSymbol, y trabajar SIEMPRE con ISymbol.
 * Recordar: Que sea ISymbol el que renderiza.
 * extensible el s?mbolo a usar. NOTA: Ver tambi?n comentario en ISymbol
 *
 * @author fjp
 */
public class FGraphicUtilities {


	/**
	 * Dibuja el s?mbolo que se le pasa como par?metro en el Graphics.
	 *
	 * @param g2 Graphics2D sobre el que dibujar.
	 * @param mT2 Matriz de transformaci?n.
	 * @param r Rect?ngulo.
	 * @param symbol S?mbolo a dibujar.
	 */
	public static void DrawSymbol(Graphics2D g2, AffineTransform mT2,
		Rectangle r, FSymbol symbol) {
		FShape shp;

		AffineTransform mT = new AffineTransform();
		mT.setToIdentity();

		Rectangle r2 = new Rectangle(r.x + 2 + (r.width / 3), r.y, r.width / 3,
				r.height);
		Rectangle r3 = new Rectangle(r.x + 2 + ((2 * r.width) / 3), r.y,
				r.width / 3, r.height);

		// g2.clearRect(r.x, r.y, r.width, r.height);
		// System.out.println("r = " + r.toString() + " Color preview:" + symbol.m_Color.toString());
		// System.out.println("symbol.m_symbolType= "+symbol.m_symbolType);
		switch (symbol.getSymbolType()) {
			case FConstant.SYMBOL_TYPE_POINT:
				shp = new FPoint2D(r.x + (r.width / 2), r.y + (r.height / 2));

				//  Para no tener que clonarlo si viene en unidades de mapa
				boolean bAux2 = symbol.isSizeInPixels();
				int alturaMetros = symbol.getSize(); // Nota: Cambiar m_Size a float

				if (!bAux2) {
					symbol.setSizeInPixels(true);
					symbol.setSize(8); // tama?o fijo
				}

				symbol.draw(g2, mT, shp, null);
				// FGraphicUtilities.DrawShape(g2, mT, shp, symbol);


				if (!bAux2) {
					symbol.setSize(alturaMetros);
					symbol.setSizeInPixels(bAux2);
				}

				if (symbol.getFont() != null) {
					// Para no tener que clonarlo si viene en unidades de mapa
					boolean bAux = symbol.isFontSizeInPixels();
					symbol.setFontSizeInPixels(true);
					FGraphicUtilities.DrawLabel(g2, mT, shp, symbol,
						new FLabel("Abcd"));
					symbol.setFontSizeInPixels(bAux);
				}

				break;

			case FConstant.SYMBOL_TYPE_LINE:

				Rectangle rect = mT2.createTransformedShape(r).getBounds();
				GeneralPathX line = new GeneralPathX();
				line.moveTo(rect.x, rect.y + (rect.height / 2));

				// line.lineTo(rect.x + rect.width/3, rect.y + rect.height);
				// line.lineTo(rect.x + 2*rect.width/3, rect.y);
				// line.lineTo(rect.x + rect.width, rect.y + rect.height/2);
				line.curveTo(rect.x + (rect.width / 3),
					rect.y + (2 * rect.height),
					rect.x + ((2 * rect.width) / 3), rect.y - rect.height,
					rect.x + rect.width, rect.y + (rect.height / 2));

				shp = new FPolyline2D(line);
				symbol.draw(g2, mT, shp, null);
				// FGraphicUtilities.DrawShape(g2, mT, shp, symbol);

				break;

			case FConstant.SYMBOL_TYPE_FILL:

				GeneralPathX rectAux = new GeneralPathX(r);
				rectAux.transform(mT2);
				shp = new FPolygon2D(rectAux);

				// System.out.println("rect = "+rectAux.getBounds());
				symbol.draw(g2, mT, shp, null);
				// FGraphicUtilities.DrawShape(g2, mT, shp, symbol);

				break;

			case FShape.MULTI:

				// Pol?gono
				r.setSize(r.width / 3, r.height);

				GeneralPathX rectAux2 = new GeneralPathX(r);
				rectAux2.transform(mT2);
				shp = new FPolygon2D(rectAux2);
				symbol.draw(g2, mT, shp, null);
				// FGraphicUtilities.DrawShape(g2, mT, shp, symbol);

				// L?nea
				rect = mT2.createTransformedShape(r2).getBounds();
				line = new GeneralPathX();
				line.moveTo(rect.x, rect.y + (rect.height / 2));

				line.curveTo(rect.x + (rect.width / 3),
					rect.y + (2 * rect.height),
					rect.x + ((2 * rect.width) / 3), rect.y - rect.height,
					rect.x + rect.width, rect.y + (rect.height / 2));

				shp = new FPolyline2D(line);
				symbol.draw(g2, mT, shp, null);
				// FGraphicUtilities.DrawShape(g2, mT, shp, symbol);

				// Punto:
				shp = new FPoint2D(r3.x + (r3.width / 2), r3.y +
						(r3.height / 2));

				//  Para no tener que clonarlo si viene en unidades de mapa
				bAux2 = symbol.isSizeInPixels();
				alturaMetros = symbol.getSize(); // Nota: Cambiar m_Size a float

				if (!bAux2) {
					symbol.setSizeInPixels(true);
					symbol.setSize(4); // tama?o fijo
				}
				symbol.draw(g2, mT, shp, null);
				// FGraphicUtilities.DrawShape(g2, mT, shp, symbol);

				if (!bAux2) {
					symbol.setSize(alturaMetros);
					symbol.setSizeInPixels(bAux2);
				}

				if (symbol.getFont() != null) {
					// Para no tener que clonarlo si viene en unidades de mapa
					boolean bAux = symbol.isFontSizeInPixels();
					symbol.setFontSizeInPixels(true);
					FGraphicUtilities.DrawLabel(g2, mT, shp, symbol,
						new FLabel("Abcd"));
					symbol.setFontSizeInPixels(bAux);
				}

				break;
			case FConstant.SYMBOL_TYPE_TEXT:
				shp = new FPoint2D(r3.x + (r3.width / 2), r3.y +
						(r3.height / 2));
				boolean bAux = symbol.isFontSizeInPixels();
				symbol.setFontSizeInPixels(true);
				FGraphicUtilities.DrawLabel(g2, mT, shp, symbol,
					new FLabel("Abcd"));
				symbol.setFontSizeInPixels(bAux);
				break;
		}
	}

	/**
	 * Dibuja el shape que se pasa como par?metro con las caracter?sticas que
	 * aporta el s?mbolo sobre el Graphics2D.
	 *
	 * @param g2 Graphics2D sobre el que dibujar.
	 * @param mT Matriz de transformaci?n.
	 * @param shp FShape a dibujar.
	 * @param theSymbol S?mbolo.
	 * @deprecated
	 */
	public static void DrawShape(Graphics2D g2, AffineTransform mT, FShape shp,
		FSymbol theSymbol) {
		// Hacemos la transformaci?n del shape aqu? dentro... por ahora.
		if (shp == null || theSymbol == null || (!theSymbol.isShapeVisible())) {
			return;
		}
        g2.setColor(theSymbol.getColor());

		/* if (shp instanceof FPolygon2D)
		   {
		           System.out.println("Entra pol?gono");
		   } */
		int type=shp.getShapeType();
		/* if (shp.getShapeType()>=FShape.Z){
			type=shp.getShapeType()-FShape.Z;
		} */
		switch (type) {
			case FShape.POINT: //Tipo punto
            case FShape.POINT + FShape.Z:
				drawSymbolPoint(g2, mT, (FPoint2D) shp, theSymbol);

				break;

			case FShape.LINE:
            case FShape.LINE + FShape.Z:
            case FShape.LINE | FShape.M: //MCoord
			case FShape.ARC:
			case FShape.ARC + FShape.Z:
				// Shape theShp = mT.createTransformedShape(shp.m_Polyline);
				// g2.setColor(theSymbol.m_Color);
				if (theSymbol.getStroke() != null) {
					g2.setStroke(theSymbol.getStroke());
				}

				g2.draw(shp);

				break;

			case FShape.POLYGON:
			case FShape.POLYGON | FShape.M:				
            case FShape.POLYGON + FShape.Z:
			case FShape.ELLIPSE:
			case FShape.ELLIPSE + FShape.Z:
            case FShape.CIRCLE:
			case FShape.CIRCLE + FShape.Z:

			    if (theSymbol.getFill() != null)
			        g2.setPaint(theSymbol.getFill());

			    if (theSymbol.getColor() != null)
				    if (theSymbol.getStyle() != FConstant.SYMBOL_STYLE_DGNSPECIAL) {
				        g2.fill(shp);
				}

				if (theSymbol.isOutlined()) {
					g2.setColor(theSymbol.getOutlineColor());

					if (theSymbol.getStroke() != null) {
						g2.setStroke(theSymbol.getStroke());
					}

					g2.draw(shp);
				}

				break;

		}
	}
//	public static double toMapDistance(AffineTransform at,int d) {
//		double dist = d / at.getScaleX();
//
//		return dist;
//	}
//	public static int fromMapDistance(AffineTransform at,double d) {
//		Point2D.Double pWorld = new Point2D.Double(1, 1);
//		Point2D.Double pScreen = new Point2D.Double();
//
//		try {
//			at.deltaTransform(pWorld, pScreen);
//		} catch (Exception e) {
//			System.err.print(e.getMessage());
//		}
//
//		return (int) (d * pScreen.x);
//	}
	/**
	 * Dibuja el FLabel que se pasa como par?metro sobre el Graphics2D.
	 *
	 * @param g2 Graphics2D sobre el que dibujar.
	 * @param mT Matriz de transformaci?n.
	 * @param shp FShape a dibujar.
	 * @param theSymbol S?mbolo para aplicar.
	 * @param theLabel FLabel que contiene el texto que se debe dibujar.
	 */
	public static void DrawLabel(Graphics2D g2, AffineTransform mT, FShape shp,
		FSymbol theSymbol, FLabel theLabel) {
		float angle;
		float x;
		float y;
		Point2D pAux = null;

		// USAR TEXTLAYOUT SI QUEREMOS PERMITIR SELECCIONAR UN TEXTO
		// Y/O EDITARLO "IN SITU"

		/* if (m_labelValues[numReg].length() > 0)
		   {
		           TextLayout layout = new TextLayout(m_labelValues[numReg], font, frc);
		           layout.draw(g2, x, y);
		   } */
		if (shp == null) {
			return;
		}

		// Las etiquetas que pongamos a nulo ser? porque no la queremos dibujar.
		// ?til para cuando queramos eliminar duplicados.
		if (theLabel.getString() == null) {
			return;
		}

		FontMetrics metrics;// = g2.getFontMetrics();
		int width;// = metrics.stringWidth(theLabel.getString());
		int height;// = metrics.getMaxAscent();

		// int height = metrics.getHeight();
		//g2.setFont(theSymbol.getFont());
		//g2.setColor(theSymbol.getFontColor());

		// Aqu? hay que mirar m_Size y m_useSize...
		if (!theSymbol.isFontSizeInPixels()) {
			// Suponemos que m_Size viene en coordenadas de mundo real
			// Esto habr? que cambiarlo. Probablemente usar Style2d de geotools en lugar
			// de FSymbol.
			// CAMBIO: La altura del texto la miramos en FLabel
			// float alturaPixels = (float) (theSymbol.m_FontSize * mT.getScaleX());
			float alturaPixels = (float) (theLabel.getHeight() * mT.getScaleX());

			/* System.out.println("m_bUseSize = " + theSymbol.m_bUseSize +
			   " Escala: " + mT.getScaleX() + " alturaPixels = " + alturaPixels); */
			if (alturaPixels < 3) {
				return; // No leemos nada
			}

			Font nuevaFuente = theSymbol.getFont().deriveFont(alturaPixels);
			g2.setFont(nuevaFuente);
			g2.setColor(theSymbol.getFontColor());
			metrics=g2.getFontMetrics();
			height= metrics.getMaxAscent();
			width = metrics.stringWidth(theLabel.getString());
		}else {
			metrics = g2.getFontMetrics();
			width = metrics.stringWidth(theLabel.getString());
			height = metrics.getMaxAscent();
			g2.setFont(theSymbol.getFont());
			g2.setColor(theSymbol.getFontColor());
		}
		int type=shp.getShapeType();
		if (shp.getShapeType()>=FShape.M){         //MCoord
			type=shp.getShapeType()-FShape.M;      //MCoord
		} else if (shp.getShapeType()>=FShape.Z){  //MCoord
			type=shp.getShapeType()-FShape.Z;      //MCoord
		}                    
		switch (type) {
			case FShape.POINT: //Tipo punto
				pAux = new Point2D.Double(((FPoint2D) shp).getX(),
						((FPoint2D) shp).getY());
				pAux = mT.transform(pAux, null);

				break;

			case FShape.LINE:

				//
				if (theLabel.getOrig() == null) // Calculamos el punto y la orientaci?n solo la primera vez
				 {
					PathLength pathLen = new PathLength(shp);

					// if (pathLen.lengthOfPath() < width / mT.getScaleX()) return;
					float midDistance = pathLen.lengthOfPath() / 2;
					pAux = pathLen.pointAtLength(midDistance);
					angle = pathLen.angleAtLength(midDistance);

					if (angle < 0) {
						angle = angle + (float) (2 * Math.PI);
					}

					if ((angle > (Math.PI / 2)) &&
							(angle < ((3 * Math.PI) / 2))) {
						angle = angle - (float) Math.PI;
					}

					theLabel.setRotation(Math.toDegrees(angle));
					theLabel.setOrig(pAux);
				}

				pAux = mT.transform(theLabel.getOrig(), null);

				// pAux = theLabel.getOrig();
				// GlyphVector theGlyphs = theSymbol.m_Font.createGlyphVector(g2.getFontRenderContext(), theLabel);
				// Shape txtShp = TextPathLayout.layoutGlyphVector(theGlyphs, shp.m_Polyline,TextPathLayout.ALIGN_MIDDLE);
				// g2.draw(txtShp);
				// System.out.println("Pintando etiqueta " + theLabel );
				break;

			case FShape.POLYGON:

				if (theLabel.getOrig() == null) // Calculamos el punto solo la primera vez
				 {
					Geometry geo = FConverter.java2d_to_jts(shp);

					// System.out.println("Area de " + m_labelValues[numReg] + " = "
					//   + geo.getArea());
					//   System.out.println(geo.toText());
					Point pJTS = geo.getInteriorPoint();
					FShape pLabel = FConverter.jts_to_java2d(pJTS);
					theLabel.setRotation(0);
					theLabel.setOrig(new Point2D.Double(
							((FPoint2D) pLabel).getX(),
							((FPoint2D) pLabel).getX()));
				}

				pAux = mT.transform(theLabel.getOrig(), null);

				break;
		}

		AffineTransform ant = g2.getTransform();

		x = (float) pAux.getX();
		y = (float) pAux.getY();

		AffineTransform Tx = (AffineTransform) ant.clone();
		Tx.translate(x, y); // S3: final translation
		Tx.rotate(theLabel.getRotation()); // S2: rotate around anchor
		g2.setTransform(Tx);

		switch (theLabel.getJustification()) {
			case FLabel.LEFT_BOTTOM:
				g2.drawString(theLabel.getString(), 0, 0 - 3);

				break;

			case FLabel.LEFT_CENTER:
				g2.drawString(theLabel.getString(), 0, 0 - (height / 2));

				break;

			case FLabel.LEFT_TOP:
				g2.drawString(theLabel.getString(), 0, 0 - height);

				break;

			case FLabel.CENTER_BOTTOM:
				g2.drawString(theLabel.getString(), 0 - (width / 2), 0 - 3);

				break;

			case FLabel.CENTER_CENTER:
				g2.drawString(theLabel.getString(), 0 - (width / 2),
					0 - (height / 2));

				break;

			case FLabel.CENTER_TOP:
				g2.drawString(theLabel.getString(), 0 - (width / 2), 0 -
					height);

				break;

			case FLabel.RIGHT_BOTTOM:
				g2.drawString(theLabel.getString(), 0 - width, 0 - 3);

				break;

			case FLabel.RIGHT_CENTER:
				g2.drawString(theLabel.getString(), 0 - width, 0 -
					(height / 2));

				break;

			case FLabel.RIGHT_TOP:
				g2.drawString(theLabel.getString(), 0 - width, 0 - height);

				break;
		}

		// Restauramos
		g2.setTransform(ant);
	}
	/**
	 * Dibuja el FLabel que se pasa como par?metro sobre el Graphics2D.
	 *
	 * @param g2 Graphics2D sobre el que dibujar.
	 * @param mT Matriz de transformaci?n.
	 * @param shp FShape a dibujar.
	 * @param theSymbol S?mbolo para aplicar.
	 * @param theLabel FLabel que contiene el texto que se debe dibujar.
	 */
	public static void DrawAnnotation(Graphics2D g2, AffineTransform at,
		FSymbol theSymbol, FLabel theLabel,FontMetrics metrics,boolean isSelected) {
		float x;
		float y;
		Point2D pAux = null;
		// Las etiquetas que pongamos a nulo ser? porque no la queremos dibujar.
		// ?til para cuando queramos eliminar duplicados.
		if (theLabel.getString() == null) {
			return;
		}

//		 Aqu? hay que mirar m_Size y m_useSize...
		if (!theSymbol.isFontSizeInPixels()) {
			// Suponemos que m_Size viene en coordenadas de mundo real
			// Esto habr? que cambiarlo. Probablemente usar Style2d de geotools en lugar
			// de FSymbol.
			// CAMBIO: La altura del texto la miramos en FLabel
			// float alturaPixels = (float) (theSymbol.m_FontSize * mT.getScaleX());
			float alturaPixels = (float) (theLabel.getHeight() * at.getScaleX()*FLabel.SQUARE);
			if (alturaPixels < 3) {
				return; // No leemos nada
			}
			Font nuevaFuente = theSymbol.getFont().deriveFont(alturaPixels);
			g2.setFont(nuevaFuente);
		}


		if (isSelected){
			g2.setColor(MapContext.getSelectionColor());
		}else{
			g2.setColor(theSymbol.getFontColor());
		}
		pAux = at.transform(theLabel.getOrig(), null);
		AffineTransform ant = g2.getTransform();

		x = (float) pAux.getX();
		y = (float) pAux.getY();

		AffineTransform Tx = (AffineTransform) ant.clone();
		Tx.translate(x, y); // S3: final translation
		Tx.rotate(theLabel.getRotation()); // S2: rotate around anchor
		g2.setTransform(Tx);


		String s=theLabel.getString();


		//switch (theLabel.getJustification()) {

		 //case FLabel.LEFT_BOTTOM:
				g2.drawString(s, 0, 0 - 3);
/*
				break;

			case FLabel.LEFT_CENTER:
				float height = metrics.getMaxAscent();
				g2.drawString(s, 0, 0 - (height / 2));

				break;

			case FLabel.LEFT_TOP:
				height = metrics.getMaxAscent();
				g2.drawString(s, 0, 0 - height);

				break;

			case FLabel.CENTER_BOTTOM:
				float width = metrics.stringWidth(s);
				g2.drawString(s, 0 - (width / 2), 0 - 3);

				break;

			case FLabel.CENTER_CENTER:
				height = metrics.getMaxAscent();
				width = metrics.stringWidth(s);
				g2.drawString(s, 0 - (width / 2),
					0 - (height / 2));

				break;

			case FLabel.CENTER_TOP:
				width = metrics.stringWidth(s);
				height = metrics.getMaxAscent();
				g2.drawString(s, 0 - (width / 2), 0 -
					height);

				break;

			case FLabel.RIGHT_BOTTOM:
				width = metrics.stringWidth(s);
				g2.drawString(s, 0 - width, 0 - 3);

				break;

			case FLabel.RIGHT_CENTER:
				width = metrics.stringWidth(s);
				height = metrics.getMaxAscent();
				g2.drawString(s, 0 - width, 0 -
					(height / 2));

				break;

			case FLabel.RIGHT_TOP:
				width = metrics.stringWidth(s);
				height = metrics.getMaxAscent();
				g2.drawString(s, 0 - width, 0 - height);

				break;
		}
		*/
		///Rectangle2D borde=vp.fromMapRectangle(theLabel.getBoundBox());//theLabel.getBoundingBox();
		///g2.setColor(Color.blue);
		///g2.drawRect((int)borde.getX(),(int)borde.getY(),(int)borde.getWidth(),(int)borde.getHeight());
		// Restauramos
		g2.setTransform(ant);
	}

	/**
	 * Dibuja un punto sobre el Graphics2D que se pasa como par?metro.
	 *
	 * @param g2 Graphics2D sobre el que dibujar.
	 * @param mT MAtriz de transformaci?n.
	 * @param pAux punto a dibujar.
	 * @param theSymbol S?mbolo a aplicar.
	 */
	private static void drawSymbolPoint(Graphics2D g2, AffineTransform mT,
		FPoint2D pAux, FSymbol theSymbol) {
		int x;
		int y;
		x = (int) pAux.getX();
		y = (int) pAux.getY();

		/*if (x==0){
		   x=100;
		   }
		   if (y==0){
		           y=100;
		   }
		 */
		Rectangle rectAux = new Rectangle();

		// Aqu? hay que mirar m_Size y m_useSize...
		float radio_simbolo;
		radio_simbolo = theSymbol.getSize() / 2;
		// theSymbol.setSizeInPixels(true);

		if (!theSymbol.isSizeInPixels()) {
			// Suponemos que m_Size viene en coordenadas de mundo real
			radio_simbolo = (float) (theSymbol.getSize() * mT.getScaleX());

			/* System.out.println("m_bUseSize = " + theSymbol.m_bUseSize +
			   " Escala: " + mT.getScaleX() + " alturaPixels = " + alturaPixels); */
			// if (radio_simbolo < 1) return; // No dibujamos nada
			rectAux.setRect(x - radio_simbolo, y - radio_simbolo,
				radio_simbolo * 2, radio_simbolo * 2);
		} else {
			// m_Size viene en pixels
			rectAux.setRect(x - radio_simbolo, y - radio_simbolo,
				theSymbol.getSize(), theSymbol.getSize());
		}

		// 	continue; //radioSimbolo_en_pixels = 3;
		if (theSymbol.getFill() != null) {
			g2.setPaint(theSymbol.getFill());
		}

		if (theSymbol.getStroke() != null) {
			g2.setStroke(theSymbol.getStroke());
		}

		if (radio_simbolo < 2) {
			g2.fillRect(rectAux.x, rectAux.y, rectAux.width, rectAux.height);

			return;
		}

		switch (theSymbol.getStyle()) {
			case FConstant.SYMBOL_STYLE_MARKER_CIRCLE: // Circulito

				if (theSymbol.getColor() != null) {
					g2.fillOval(rectAux.x, rectAux.y, rectAux.width,
						rectAux.height);
				}

				if (theSymbol.isOutlined()) {
					g2.setColor(theSymbol.getOutlineColor());
					g2.drawOval(rectAux.x, rectAux.y, rectAux.width,
						rectAux.height);
				}

				break;

			case FConstant.SYMBOL_STYLE_MARKER_SQUARE: // Cuadrado
			case FConstant.SYMBOL_STYLE_FILL_SOLID:
				g2.fillRect(rectAux.x, rectAux.y, rectAux.width, rectAux.height);

				if (theSymbol.isOutlined()) {
					g2.setColor(theSymbol.getOutlineColor());
					g2.drawRect(rectAux.x, rectAux.y, rectAux.width,
						rectAux.height);
				}

				break;

			case FConstant.SYMBOL_STYLE_MARKER_TRIANGLE: // Triangulo

				// y = r*sin30, x = r*cos30
				GeneralPathX genPath = new GeneralPathX();
				genPath.moveTo(x - (int) (radio_simbolo * 0.866),
					y + (int) (radio_simbolo * 0.5));
				genPath.lineTo(x + (int) (radio_simbolo * 0.866),
					y + (int) (radio_simbolo * 0.5));
				genPath.lineTo(x, y - (float) radio_simbolo);
				genPath.closePath();

				g2.fill(genPath);

				break;

			case FConstant.SYMBOL_STYLE_MARKER_CROSS: // cruz
			case FConstant.SYMBOL_STYLE_DGNSPECIAL: // Cruz

				GeneralPathX genPathCruz = new GeneralPathX();
				genPathCruz.moveTo(x, y - radio_simbolo);
				genPathCruz.lineTo(x, y + radio_simbolo);
				genPathCruz.moveTo(x - radio_simbolo, y);
				genPathCruz.lineTo(x + radio_simbolo, y);
				g2.draw(genPathCruz);

				break;

			case 34: // TrueType marker

			/* lf.lfHeight = -radioSimbolo_en_pixels;
			   angulo = pSimbolo->m_Rotation;  // En radianes, de -pi a pi
			   angulo = -180.0 * angulo / PI;

			   lf.lfEscapement = (long) angulo*10;
			   lf.lfOrientation = (long) angulo*10;

			   fuente.CreateFontIndirect(&lf);
			   pOldFont = pDC->SelectObject(&fuente);

			   pDC->TextOut(pAPI.x, pAPI.y+radioSimbolo_en_pixels/2,elChar,1);

			   pDC->SelectObject(pOldFont);
			   fuente.DeleteObject();

			   break; */
			case FConstant.SYMBOL_STYLE_MARKER_IMAGEN: // Icono
			 {
				if (theSymbol.getIcon() != null) {
					float w;
					float h;

					if (!theSymbol.isSizeInPixels()) {
						// Suponemos que m_Size viene en coordenadas de mundo real
						// Por ejemplo, nos valemos del ancho para fijar la escala
						w = (float) (theSymbol.getSize() * mT.getScaleX());
						h = (theSymbol.getIcon().getHeight(null) * w) / theSymbol.getIcon()
																				 .getWidth(null);

						rectAux.setRect(x - w, y - h, w * 2, h * 2);
					} else {
						// m_Size viene en pixels
						w = theSymbol.getSize();
						h = (theSymbol.getIcon().getHeight(null) * w) / theSymbol.getIcon()
																				 .getWidth(null);
						rectAux.setRect(x - w/2, y - h/2, w, h);
					}
					/* if (theSymbol.getImgObserver() != null)
					{
						g2.setColor(Color.WHITE);
						g2.fillRect(rectAux.x, rectAux.y, rectAux.width, rectAux.height);
					} */
					g2.drawImage(theSymbol.getIcon(), rectAux.x, rectAux.y,
						rectAux.width, rectAux.height, theSymbol.getImgObserver());
				} else {
					String strImg = "Image"; // Utilities.getMessage(FGraphicUtilities.class,"imagen");
					FontMetrics metrics = g2.getFontMetrics();
					int width = metrics.stringWidth(strImg);
					int height = metrics.getMaxAscent();

					g2.drawString(strImg, x - (width / 2), y - 2 +
						(height / 2));
				}

				break;
			}

			/* DrawIconEx(pDC->m_hDC, pAPI.x-(pSimbolo->m_widthIco/2), pAPI.y-(pSimbolo->m_heightIco/2),
			   pSimbolo->m_hIcon, pSimbolo->m_widthIco, pSimbolo->m_heightIco, 0 , NULL, DI_NORMAL);
			   break; */
			case FConstant.SYMBOL_STYLE_POINTZ: // Circulito

				if (theSymbol.getColor() != null) {
					g2.fillOval(rectAux.x, rectAux.y, rectAux.width,
						rectAux.height);
				}

				if (theSymbol.isOutlined()) {
					g2.setColor(theSymbol.getOutlineColor());
					g2.drawOval(rectAux.x, rectAux.y, rectAux.width,
						rectAux.height);
				}

				break;
		} // del switch estilo
	}
	public static void DrawHandlers(Graphics2D g, AffineTransform at,
			Handler[] handlers,ISymbol symbol) {

			for (int i = 0; i < handlers.length; i++) {
				Point2D point = handlers[i].getPoint();
				at.transform(point, point);
				g.setColor(((IFillSymbol)symbol).getFillColor());
				g.fillRect((int) (point.getX() - 3), (int) (point.getY() - 3), 7, 7);
				g.setColor(((ILineSymbol)symbol).getColor());
				g.drawRect((int) (point.getX() - 5), (int) (point.getY() - 5), 10, 10);
				g.drawString( "" + i, (int) (point.getX() - 5), (int) (point.getY() - 5));
			}
		}

	public static void DrawVertex(Graphics2D g, AffineTransform at, Handler handlers) {
		//for (int i = 0; i < handlers.length; i++) {
			Point2D point = handlers.getPoint();
			at.transform(point, point);
			g.setColor(Color.black);
			g.drawLine((int)point.getX()-2,(int)point.getY()-10,(int)point.getX()-2,(int)point.getY()+10);
			g.drawLine((int)point.getX()+2,(int)point.getY()-10,(int)point.getX()+2,(int)point.getY()+10);
			g.drawLine((int)point.getX()-10,(int)point.getY()-2,(int)point.getX()+10,(int)point.getY()-2);
			g.drawLine((int)point.getX()-10,(int)point.getY()+2,(int)point.getX()+10,(int)point.getY()+2);
			g.setColor(Color.red);
			g.drawLine((int)point.getX()-1,(int)point.getY()-10,(int)point.getX()-1,(int)point.getY()+10);
			g.drawLine((int)point.getX()+1,(int)point.getY()-10,(int)point.getX()+1,(int)point.getY()+10);
			g.drawLine((int)point.getX()-10,(int)point.getY()-1,(int)point.getX()+10,(int)point.getY()-1);
			g.drawLine((int)point.getX()-10,(int)point.getY()+1,(int)point.getX()+10,(int)point.getY()+1);

		//}
	}
}

