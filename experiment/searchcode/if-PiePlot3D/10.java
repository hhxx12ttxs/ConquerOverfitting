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

import org.apache.log4j.Logger;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.plot.Plot;
import org.jfree.data.general.DefaultKeyedValuesDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.util.Rotation;

import com.iver.cit.gvsig.fmap.core.symbols.ISymbol;
import com.iver.cit.gvsig.fmap.core.symbols.SymbolDrawingException;
import com.iver.utiles.XMLEntity;

public class PieChart3DSymbol extends JFreeChartSymbol {
	private boolean clockwise;
    private String[] keys;
    private double[] values;
    private DefaultKeyedValuesDataset dataset;
    private boolean circular;
    private double minimumAngleToDraw;
    private double depthFactor;
    private boolean ignoreZeroValues;
    private float foregroundAlpha;
	

   	private void updateDataset() {
		if (dataset == null) dataset = new DefaultKeyedValuesDataset();
    	else dataset.clear();

    	for (int i = 0; keys != null &&  i < keys.length; i++) {
    		dataset.insertValue(i, keys[i], values[i]);
    	}
	}

   
    
    protected Plot getMapPlot() {
    	if (mapPlot == null) {
			PiePlot3D myMapPlot;
    		try {
				myMapPlot = (PiePlot3D) getOutlinePlot().clone();
        	} catch (CloneNotSupportedException e) {
				Logger.getLogger(getClass()).error("Error cloning the PieChartSymbol's plot, check consistency");
				myMapPlot = new PiePlot3D();
			}
    	
        	myMapPlot.setBackgroundPaint(null);
        	myMapPlot.setOutlinePaint(null);
        	myMapPlot.setLabelBackgroundPaint(null);
        	myMapPlot.setLabelGenerator(null); 
        	myMapPlot.setLabelLinksVisible(false);
        	mapPlot = myMapPlot;
    	} 
		return mapPlot;
	}

	protected Plot getOutlinePlot() {
		if (outlinePlot == null) {
			PiePlot3D myMapPlot = new PiePlot3D();
    		
        	myMapPlot.setLabelGap(0);
        	myMapPlot.setIgnoreZeroValues(ignoreZeroValues);
			
			updateDataset();
	    	
			
			myMapPlot.setDataset(new DefaultPieDataset(dataset));
			myMapPlot.setDirection(clockwise ? Rotation.CLOCKWISE : Rotation.ANTICLOCKWISE);
			myMapPlot.setDepthFactor(depthFactor);
			myMapPlot.setMinimumArcAngleToDraw(minimumAngleToDraw);
			myMapPlot.setCircular(circular); 
			myMapPlot.setStartAngle(getRotation());
			/*
			myMapPlot.setDarkerSides(false); // requires jfreechart 1.0.10
			myMapPlot.setForegroundAlpha(foregroundAlpha); // requires jfreechart 1.0.10
			*/
			outlinePlot = myMapPlot;
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
		xml.putProperty("circular", circular);
		xml.putProperty("minAngleToDraw", minimumAngleToDraw);
		xml.putProperty("depthFactor", depthFactor);
		xml.putProperty("ignoreZeroValues", ignoreZeroValues);
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
		setCircular(xml.getBooleanProperty("circular"));
		setMinimumAngleToDraw(xml.getDoubleProperty("minAngleToDraw"));
		setDepthFactor(xml.getDoubleProperty("depthFactor"));
		setIgnoreZeroValues(xml.getBooleanProperty("ignoreZeroValues"));
		setForegroundAlpha(xml.getFloatProperty("foregroundAlpha"));
	}

	public void setIgnoreZeroValues(boolean ignoreZeroValues) {
		this.ignoreZeroValues = ignoreZeroValues;
	}
	
	public boolean isIgnoreZeroValues() {
		return ignoreZeroValues;
	}

	public String getClassName() {
		return getClass().getName();
	}

	public double[] getValues() {
		return values;
	}
	
	public void setValues(double[] values) {
		this.values = values;
	}
	
	public String[] getKeys() {
		return keys;
	}
	
	public void setKeys(String[] keys) {
		this.keys = keys;
	}

	public boolean isClockwise() {
		return clockwise;
	}

	public void setClockwise(boolean clockwise) {
		this.clockwise = clockwise;
	}

	public boolean isCircular() {
		return circular;
	}

	public void setCircular(boolean circular) {
		this.circular = circular;
	}

	public double getMinimumAngleToDraw() {
		return minimumAngleToDraw;
	}

	public void setMinimumAngleToDraw(double minimumAngleToDraw) {
		this.minimumAngleToDraw = minimumAngleToDraw;
	}

	public double getDepthFactor() {
		return depthFactor;
	}

	public void setDepthFactor(double depthFactor) {
		this.depthFactor = depthFactor;
	}



	public float getForegroundAlpha() {
		return foregroundAlpha;
	}



	public void setForegroundAlpha(float foregroundAlpha) {
		this.foregroundAlpha = foregroundAlpha;
	}

}

