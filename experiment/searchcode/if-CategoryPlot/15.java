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
import java.awt.geom.AffineTransform;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryAxis3D;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis3D;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.BarRenderer3D;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import com.iver.cit.gvsig.fmap.core.FPoint2D;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.utiles.XMLEntity;
import com.iver.utiles.swing.threads.Cancellable;

public class BarChart3DSymbol extends JFreeChartSymbol {
	private String[] rowKeys;
	private String[] columnKeys;
	private double[] values;
	private float foregroundAlpha;
	private ChartRenderingInfo chRndrNfo;
	private String categoryAxisLabel;
	private String valueAxisLabel;
	private boolean drawBarOutline;
	private boolean vertical;

	protected Plot getMapPlot() {
		mapPlot = null; // <- delete this
		if (mapPlot == null) {
			CategoryPlot myMapPlot = new CategoryPlot();
			try {
				myMapPlot = (CategoryPlot) getOutlinePlot().clone();
			} catch (CloneNotSupportedException e) {
				Logger.getLogger(getClass()).
					error("Error cloning the BarChart3DSymbol's plot, check consistency");
				myMapPlot = new CategoryPlot();
			}

			myMapPlot.setBackgroundPaint(null);
			myMapPlot.setOutlinePaint(null);
			BarRenderer3D renderer = (BarRenderer3D) myMapPlot.getRenderer();
			renderer.setDrawBarOutline(drawBarOutline);
//			renderer.setBaseItemLabelsVisible(false, false);
//			renderer.setBaseSeriesVisible(false, false);
			renderer.setBaseSeriesVisibleInLegend(false);
			
			
//myMapPlot.setBackgroundImage(null);
			
			mapPlot = myMapPlot;
		} 
		return mapPlot;
	}

	@Override
	public void draw(Graphics2D g, AffineTransform affineTransform, FShape shp,
			Cancellable cancel) {
		FPoint2D p = (FPoint2D) shp;
		double size = getSize();
		double halfSize = size*0.5;
		double minx = p.getX() - halfSize;
		double miny = p.getY() - halfSize;
		super.draw(g, affineTransform, shp, cancel);
	}
	
	protected Plot getOutlinePlot() {
		outlinePlot = null; // <- delete this
		if (outlinePlot == null) {
			CategoryPlot myOutlinePlot;
			DefaultCategoryDataset dataset = new DefaultCategoryDataset();
			
			dataset.addValue(23D, "Series 1", "London");
//			dataset.addValue(14D, "Series 1", "New York");
//			dataset.addValue(14D, "Series 1", "Istanbul");
//			dataset.addValue(14D, "Series 1", "Cairo");
			dataset.addValue(13D, "Series 2", "London");
//			dataset.addValue(19D, "Series 2", "New York");
//			dataset.addValue(19D, "Series 2", "Istanbul");
//			dataset.addValue(19D, "Series 2", "Cairo");
			dataset.addValue(7D, "Series 3", "London");
//			dataset.addValue(9D, "Series 3", "New York");
//			dataset.addValue(9D, "Series 3", "Istanbul");
//			dataset.addValue(9D, "Series 3", "Cairo");

			
				CategoryAxis categoryAxis = new CategoryAxis3D(categoryAxisLabel);
				ValueAxis valueAxis = new NumberAxis3D(valueAxisLabel);

				BarRenderer3D renderer = new BarRenderer3D();
				myOutlinePlot = new CategoryPlot(dataset, categoryAxis, valueAxis, renderer);
				double min = Double.POSITIVE_INFINITY;
				double max = Double.NEGATIVE_INFINITY;
//				for (int i = 0; values != null && i < values.length; i++) {
//				min = Math.min(min, values[i]);
//				max = Math.min(max, values[i]);
//				dataset.addValue(values[i], rowKeys[i], columnKeys[i]);	
//				}
				myOutlinePlot.setDataset(dataset);
				myOutlinePlot.setOrientation(vertical ? PlotOrientation.VERTICAL : PlotOrientation.HORIZONTAL);
				outlinePlot = myOutlinePlot;

			
		}
		return outlinePlot;
	}


	public XMLEntity getXMLEntity() {
		XMLEntity xml = new XMLEntity();
		xml.putProperty("className", getClassName());
		xml.putProperty("desc", getDescription());
		xml.putProperty("isShapeVisible", isShapeVisible());
		xml.putProperty("unit", getUnit());
		xml.putProperty("referenceSystem", getReferenceSystem());
		xml.putProperty("size", getSize());
		xml.putProperty("rotation", getRotation());
		xml.putProperty("foregroundAlpha", foregroundAlpha);
		return xml;
	}

	public void setXMLEntity(XMLEntity xml) {
		setDescription(xml.getStringProperty("desc"));
		setIsShapeVisible(xml.getBooleanProperty("isShapeVisible"));
		setUnit(xml.getIntProperty("unit"));
		setReferenceSystem(xml.getIntProperty("referenceSystem"));
		setSize(xml.getDoubleProperty("size"));
		setRotation(xml.getDoubleProperty("rotation"));
		
	}

	public String getClassName() {
		return getClass().getName();
	}

	public String[] getRowKeys() {
		return rowKeys;
	}

	public void setRowKeys(String[] rowKeys) {
		this.rowKeys = rowKeys;
	}

	public String[] getColumnKeys() {
		return columnKeys;
	}

	public void setColumnKeys(String[] columnKeys) {
		this.columnKeys = columnKeys;
	}

	public double[] getValues() {
		return values;
	}

	public void setValues(double[] values) {
		this.values = values;
	}
}

