/*
 * Created on 06-nov-2006
 *
 * gvSIG. Sistema de Informaci?n Geogr?fica de la Generalitat Valenciana
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
/* CVS MESSAGES:
 *
 * $Id: EdgesMemoryDriver.java 22182 2008-07-10 07:20:11Z fpenarrubia $
 * $Log$
 * Revision 1.3  2007-09-17 11:44:08  fjp
 * Primera compilaci?n que se puede ejecutar con el HEAD. Falta revisar algo del etiquetado, y poner c?digo en el isEnabled e isVisible, pero con esto ya se puede empezar a trabajar.
 *
 * Revision 1.2  2007/09/07 11:29:47  fjp
 * Casi compila. Falta arreglar lo de FArrowSymbol y retocar el graphiclist de FMap.
 *
 * Revision 1.1.2.2  2007/08/09 12:18:27  fjp
 * Area de influencia con leyenda
 *
 * Revision 1.1.2.1  2007/08/08 11:43:40  fjp
 * Principio de giros y area de influencia
 *
 * Revision 1.2  2006/11/08 19:32:36  azabala
 * *** empty log message ***
 *
 * Revision 1.1  2006/11/07 19:49:28  azabala
 * *** empty log message ***
 *
 *
 */
package org.gvsig.graph.solvers;

import java.awt.Color;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.sql.Types;
import java.text.NumberFormat;

import org.gvsig.graph.core.GvEdge;
import org.gvsig.graph.core.GvNode;
import org.gvsig.graph.core.Network;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.data.DataSourceFactory;
import com.hardcode.gdbms.engine.data.driver.ObjectDriver;
import com.hardcode.gdbms.engine.data.edition.DataWare;
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.GeneralPathX;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.ShapeFactory;
import com.iver.cit.gvsig.fmap.core.symbols.ILineSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.SimpleLineSymbol;
import com.iver.cit.gvsig.fmap.drivers.BoundedShapes;
import com.iver.cit.gvsig.fmap.drivers.DriverAttributes;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.drivers.VectorialDriver;
import com.iver.cit.gvsig.fmap.drivers.WithDefaultLegend;
import com.iver.cit.gvsig.fmap.rendering.FInterval;
import com.iver.cit.gvsig.fmap.rendering.ILegend;
import com.iver.cit.gvsig.fmap.rendering.VectorialIntervalLegend;
import com.iver.cit.gvsig.fmap.rendering.styling.labeling.ILabelingStrategy;
import com.iver.cit.gvsig.project.documents.view.legend.gui.AttrInTableLabeling;

/**
 * Driver wrapper around arcs from a network. It may be useful to show the arcs
 * as a layer without consuming more memory. Maybe it could be useful also to
 * have an Edges version instead of Arcs.
 * 
 * @author Fco. Jos? Pe?arrubia
 * 
 */
public class EdgesMemoryDriver implements VectorialDriver, ObjectDriver, BoundedShapes, 
		WithDefaultLegend {
	static FieldDescription[] fields = new FieldDescription[6];
	static {
		FieldDescription fieldDesc = new FieldDescription();
		fieldDesc.setFieldName("IDARC");
		fieldDesc.setFieldType(Types.INTEGER);
		fieldDesc.setFieldLength(20);
		fieldDesc.setFieldDecimalCount(0);
		fields[0] = fieldDesc;

		fieldDesc = new FieldDescription();
		fieldDesc.setFieldName("WEIGHT");
		fieldDesc.setFieldType(Types.DOUBLE);
		fieldDesc.setFieldLength(20);
		fieldDesc.setFieldDecimalCount(5);
		fields[1] = fieldDesc;

		fieldDesc = new FieldDescription();
		fieldDesc.setFieldName("LENGTH");
		fieldDesc.setFieldType(Types.DOUBLE);
		fieldDesc.setFieldLength(20);
		fieldDesc.setFieldDecimalCount(5);
		fields[2] = fieldDesc;

		fieldDesc = new FieldDescription();
		fieldDesc.setFieldName("TEXT");
		fieldDesc.setFieldType(Types.VARCHAR);
		fieldDesc.setFieldLength(25);
		fieldDesc.setFieldDecimalCount(0);
		fields[3] = fieldDesc;

		fieldDesc = new FieldDescription();
		fieldDesc.setFieldName("COSTN1");
		fieldDesc.setFieldType(Types.DOUBLE);
		fieldDesc.setFieldLength(20);
		fieldDesc.setFieldDecimalCount(5);
		fields[4] = fieldDesc;

		fieldDesc = new FieldDescription();
		fieldDesc.setFieldName("COSTN2");
		fieldDesc.setFieldType(Types.DOUBLE);
		fieldDesc.setFieldLength(20);
		fieldDesc.setFieldDecimalCount(5);
		fields[5] = fieldDesc;

	}

	Network net;
	private AttrInTableLabeling labeling;

	Rectangle2D fullExtent;

	public EdgesMemoryDriver(Network net) {
		this.net = net;
		labeling = new AttrInTableLabeling();
		
	}

	public int getShapeType() {
		return FShape.LINE;
	}

	public String getName() {
		return "EdgeDriver";
	}

	public DriverAttributes getDriverAttributes() {
		return null;
	}

	public boolean isWritable() {
		return false;
	}

	public int[] getPrimaryKeys()
			 {
		return null;
	}

	public void write(DataWare dataWare)
			 {
	}

	/**
	 * Returns de field type of the specified field index.
	 * 
	 * @return field type of i field
	 */
	public int getFieldType(int i) {
		return fields[i].getFieldType();
	}

	public int getShapeCount() {
		return net.getGraph().numEdges();
	}

	public Rectangle2D getFullExtent() {
		try {
			return net.getLayer().getFullExtent();
		} catch (ExpansionFileReadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ReadDriverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public IGeometry getShape(int index) {
		GvEdge edge = net.getGraph().getEdgeByID(index);
		GvNode n1 = net.getGraph().getNodeByID(edge.getIdNodeOrig());
		GvNode n2 = net.getGraph().getNodeByID(edge.getIdNodeEnd());
		Line2D.Double line = new Line2D.Double(n1.getX(), n1.getY(), n2.getX(),
				n2.getY());
		return ShapeFactory.createPolyline2D(new GeneralPathX(line));
	}

	public void reload() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hardcode.gdbms.engine.data.ReadDriver#getFieldValue(long, int)
	 */
	public Value getFieldValue(long rowIndex, int fieldId)
			 {
		Value val = ValueFactory.createNullValue();
		GvEdge edge = net.getGraph().getEdgeByID((int) rowIndex);
		switch (fieldId) {
		case 0: // idArc
			return ValueFactory.createValue(edge.getIdArc());
		case 1: // weight
			return ValueFactory.createValue(edge.getWeight());
		case 2: // length
			return ValueFactory.createValue(edge.getDistance());
		case 3:
			return ValueFactory.createValue(edge.getType() + "");
		case 4: // cost node1
			GvNode n1 = net.getGraph().getNodeByID(edge.getIdNodeOrig());

			if (n1.getBestCost() == Double.MAX_VALUE)
				return ValueFactory.createValue(-1.0);
			else
				return ValueFactory.createValue(n1.getBestCost());
		case 5: // cost node2
			GvNode n2 = net.getGraph().getNodeByID(edge.getIdNodeOrig());
			if (n2.getBestCost() == Double.MAX_VALUE)
				return ValueFactory.createValue(-1.0);
			else
				return ValueFactory.createValue(n2.getBestCost());

		}
		return val;
	}

	public int getFieldCount() {
		return fields.length;
	}

	public String getFieldName(int fieldId) {
		return fields[fieldId].getFieldName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hardcode.gdbms.engine.data.ReadDriver#getRowCount()
	 */
	public long getRowCount() {
		return net.getGraph().numEdges();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hardcode.gdbms.engine.data.driver.GDBMSDriver#setDataSourceFactory(com.hardcode.gdbms.engine.data.DataSourceFactory)
	 */
	public void setDataSourceFactory(DataSourceFactory dsf) {
	}

	public int getFieldWidth(int fieldId) {
		return fields[fieldId].getFieldLength();
	}

	private FInterval[] calculateEqualIntervals(int numIntervals,
			double minValue, double maxValue) {
		FInterval[] theIntervalArray = new FInterval[numIntervals];
		double step = (maxValue - minValue) / numIntervals;

		if (numIntervals > 1) {
			theIntervalArray[0] = new FInterval(minValue, minValue + step);

			for (int i = 1; i < (numIntervals - 1); i++) {
				theIntervalArray[i] = new FInterval(minValue + (i * step)
						+ 0.01, minValue + ((i + 1) * step));
			}

			theIntervalArray[numIntervals - 1] = new FInterval(minValue
					+ ((numIntervals - 1) * step) + 0.01, maxValue);
		} else {
			theIntervalArray[0] = new FInterval(minValue, maxValue);
		}

		return theIntervalArray;
	}

	public ILegend getDefaultLegend() {
		VectorialIntervalLegend legend = new VectorialIntervalLegend(
				FShape.LINE);
		String[] fieldNames = new String[1];
		fieldNames[0] = "COSTN1";
		legend.setClassifyingFieldNames(fieldNames);
		legend.setStartColor(Color.RED);
		legend.setEndColor(Color.BLUE);
		FInterval[] arrayIntervalos = calculateEqualIntervals(20, 0, 20000);
            FInterval elIntervalo;
            NumberFormat.getInstance().setMaximumFractionDigits(2);

            int r;
            int g;
            int b;
            int stepR;
            int stepG;
            int stepB;
            r = legend.getStartColor().getRed();
            g = legend.getStartColor().getGreen();
            b = legend.getStartColor().getBlue();
            stepR = (legend.getEndColor().getRed() - r) / arrayIntervalos.length;
            stepG = (legend.getEndColor().getGreen() - g) / arrayIntervalos.length;
            stepB = (legend.getEndColor().getBlue() - b) / arrayIntervalos.length;
		
        for (int k = 0; k < arrayIntervalos.length; k++) {
            elIntervalo = arrayIntervalos[k];

            // clave = elIntervalo; // elIntervalo.getMin() + " - " +
			// elIntervalo.getMax();
            // System.out.println("k = " + k + " clave = " + clave);
            // //Comprobar que no esta repetido y no hace falta introducir en el
			// hashtable el campo junto con el simbolo.
            // if (!m_Renderer.m_symbolList.containsKey(elIntervalo)) {
            // si no esta creado el simbolo se crea
//            FSymbol theSymbol = new FSymbol(FShape.LINE, new Color(r, g, b));
            ILineSymbol theSymbol = new SimpleLineSymbol();
            theSymbol.setLineColor(new Color(r, g, b));
            theSymbol.setDescription(NumberFormat.getInstance().format(elIntervalo.getMin()) +
                " - " +
                NumberFormat.getInstance().format(elIntervalo.getMax()));

            // ////////////////////////////////////
            // CALCULAMOS UN COLOR APROPIADO
            r = r + stepR;
            g = g + stepG;
            b = b + stepB;

            // ///////////////////////////////
            legend.addSymbol(elIntervalo, theSymbol);
        } // for

		return legend;
	}

	public ILabelingStrategy getDefaultLabelingStrategy() {
		return (ILabelingStrategy) labeling;
	}

	public Rectangle2D getShapeBounds(int index) throws ReadDriverException, ExpansionFileReadException {
		GvEdge edge = net.getGraph().getEdgeByID(index);
		GvNode n1 = net.getGraph().getNodeByID(edge.getIdNodeOrig());
		GvNode n2 = net.getGraph().getNodeByID(edge.getIdNodeEnd());
		Rectangle2D bound = new Rectangle2D.Double(n1.getX(), n1.getY(), n2.getX()-n1.getX(),
				n2.getY() - n1.getY());
		return bound;
	}

	public int getShapeType(int index) throws ReadDriverException {
		return getShapeType();
	}


}

