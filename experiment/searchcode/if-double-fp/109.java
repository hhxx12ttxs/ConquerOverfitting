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
package org.gvsig.symbology.fmap.symbols;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.print.attribute.PrintRequestAttributeSet;

import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.PlotState;

import com.iver.cit.gvsig.fmap.core.FPoint2D;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.symbols.AbstractMarkerSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.ISymbol;
import com.iver.cit.gvsig.fmap.core.symbols.SymbolDrawingException;
import com.iver.utiles.swing.threads.Cancellable;

public abstract class JFreeChartSymbol extends AbstractMarkerSymbol implements IChartSymbol{
	

	protected Plot renderPlot;
	protected Plot mapPlot;
	protected Plot outlinePlot;
	protected PlotState plotState;
	protected PlotRenderingInfo plotRenderingInfo;
	
	public ISymbol getSymbolForSelection() {
		return this;
	}

	public void draw(Graphics2D g, AffineTransform affineTransform, FShape shp,
			Cancellable cancel) {
		FPoint2D fp = (FPoint2D) shp;
		Point2D p = new Point2D.Double(fp.getX(), fp.getY());
		if (renderPlot == null) {
			renderPlot = getMapPlot();
		}
		double size = getSize();
		double halfSize = size*0.5;
		double minx = p.getX() - halfSize;
		double miny = p.getY() - halfSize;
		renderPlot.draw(g, new Rectangle2D.Double(minx, miny, size, size), p, plotState, plotRenderingInfo);
	}
	
	@Override
	public void drawInsideRectangle(Graphics2D g, AffineTransform scaleInstance, Rectangle r, PrintRequestAttributeSet properties)
	throws SymbolDrawingException {
		renderPlot = getOutlinePlot();
		super.drawInsideRectangle(g, scaleInstance, r, properties);
		renderPlot = getMapPlot();
	}
	
	
	
	protected abstract Plot getMapPlot();
	protected abstract Plot getOutlinePlot();
}

