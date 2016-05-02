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
package com.iver.cit.gvsig.graph.core.writers;

import java.sql.Types;
import java.util.Hashtable;

import com.hardcode.gdbms.engine.values.NumericValue;
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.cit.gvsig.fmap.DriverException;
import com.iver.cit.gvsig.fmap.core.DefaultRow;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.drivers.DriverIOException;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.drivers.ITableDefinition;
import com.iver.cit.gvsig.fmap.drivers.TableDefinition;
import com.iver.cit.gvsig.fmap.edition.DefaultRowEdited;
import com.iver.cit.gvsig.fmap.edition.EditionException;
import com.iver.cit.gvsig.fmap.edition.IRowEdited;
import com.iver.cit.gvsig.fmap.edition.IWriter;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
import com.iver.cit.gvsig.fmap.layers.VectorialAdapter;
import com.iver.cit.gvsig.graph.core.NodeGv;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class NetworkGvTableWriter extends AbstractNetworkWriter {
	
	private IWriter nodeWriter;

	private IWriter edgeWriter;

	private void createFields() {
		// Set up fields for table nodes
		nodeFields = new FieldDescription[3];
		FieldDescription fieldNodeId = new FieldDescription();
		fieldNodeId.setFieldName("NODEID");
		fieldNodeId.setFieldType(Types.INTEGER);

		FieldDescription fieldX = new FieldDescription();
		fieldX.setFieldName("X");
		fieldX.setFieldType(Types.DOUBLE);
		fieldX.setFieldDecimalCount(2);

		FieldDescription fieldY = new FieldDescription();
		fieldY.setFieldName("Y");
		fieldY.setFieldType(Types.DOUBLE);
		fieldY.setFieldDecimalCount(2);
		
		nodeFields[0] = fieldNodeId;
		nodeFields[1] = fieldX;
		nodeFields[2] = fieldY;

		
		// Set up fields for table edges
		edgeFields = new FieldDescription[7];

		// ID_TRAMO ORIGINAL!!!
		FieldDescription fieldArcID = new FieldDescription();
		fieldArcID.setFieldName("ArcID");
		fieldArcID.setFieldType(Types.INTEGER);

		FieldDescription fieldDirection = new FieldDescription();
		fieldDirection.setFieldName("Direction");
		fieldDirection.setFieldType(Types.SMALLINT);
		
		FieldDescription fieldNodeOrigin = new FieldDescription();
		fieldNodeOrigin.setFieldName("NodeOrigin");
		fieldNodeOrigin.setFieldType(Types.INTEGER);
		FieldDescription fieldNodeEnd = new FieldDescription();
		fieldNodeEnd.setFieldName("NodeEnd");
		fieldNodeEnd.setFieldType(Types.INTEGER);

		FieldDescription fieldType = new FieldDescription();
		fieldType.setFieldName("Type");
		fieldType.setFieldType(Types.SMALLINT);

		FieldDescription fieldDistanceDist = new FieldDescription();
		fieldDistanceDist.setFieldName("Dist");
		fieldDistanceDist.setFieldType(Types.DOUBLE);
		
		FieldDescription fieldDistanceCost = new FieldDescription();
		fieldDistanceCost.setFieldName("Cost");
		fieldDistanceCost.setFieldType(Types.DOUBLE);
		
		edgeFields[0] = fieldArcID;
		edgeFields[1] = fieldDirection;
		edgeFields[2] = fieldNodeOrigin;
		edgeFields[3] = fieldNodeEnd;
		edgeFields[4] = fieldType;
		edgeFields[5] = fieldDistanceDist;
		edgeFields[6] = fieldDistanceCost;
		
	}
	public void writeNetwork() throws EditionException, DriverException {
		double distance;
		double cost;
		short arcType;
		int direction;
		int i;
		int idNodo1, idNodo2, nodeCount, edgeCount;
		short sentidoDigit; // => 1 en esa direcci?n. 0=> Al contrario. SOLO
		// SE UTILIZA PARA LOS CALCULOS POR IDTRAMO Y
		// PORCENTAJE
		// PARA SABER SI EST? M?S CERCA DE UN NODO O DEL OTRO.

		createFields();
		
		VectorialAdapter adapter = (VectorialAdapter) lyr.getSource();

		try {
			int numEntities = adapter.getShapeCount();
			Hashtable nodeHash = new Hashtable();

			SelectableDataSource sds = lyr.getRecordset();
			
			int senseFieldIndex = -1;
			int distFieldIndex = -1;
			int typeFieldIndex = -1;
			int costFieldIndex = -1;
			
			if (fieldSense != null)
				senseFieldIndex = sds.getFieldIndexByName(fieldSense);
			if (fieldDist != null)
				distFieldIndex = sds.getFieldIndexByName(fieldDist);
			if (fieldType != null)
				typeFieldIndex = sds.getFieldIndexByName(fieldType);
			if (fieldCost != null)
				costFieldIndex = sds.getFieldIndexByName(fieldCost);

			// We create a table definition for node table.
			ITableDefinition nodesTableDef = new TableDefinition();
			nodesTableDef.setFieldsDesc(nodeFields);
			nodesTableDef.setName("Nodes");

			// We create a table definition for edges table.
			ITableDefinition edgeTableDef = new TableDefinition();
			edgeTableDef.setFieldsDesc(edgeFields);
			edgeTableDef.setName("Edges");

			nodeWriter.initialize(nodesTableDef);
			edgeWriter.initialize(edgeTableDef);
			
			nodeWriter.preProcess();
			edgeWriter.preProcess();
			
			edgeCount = 0;
			nodeCount = 0; 
			
			NumericValue valAux = null;

			for (i = 0; i < numEntities; i++) {
				IGeometry geom = adapter.getShape(i);
				Geometry jtsGeom = geom.toJTSGeometry();
				Coordinate[] coords = jtsGeom.getCoordinates();
				Coordinate c1 = coords[0];
				Coordinate c2 = coords[coords.length - 1];

				NodeGv nodeAux;
				if (!nodeHash.containsKey(c1)) // No est?.
				{
					idNodo1 = nodeCount++;
					nodeAux = new NodeGv(c1, idNodo1);
					nodeHash.put(c1, nodeAux);
					writeNode(nodeAux);
				} else {
					nodeAux = (NodeGv) nodeHash.get(c1);
				}
				idNodo1 = nodeAux.getId().intValue();

				if (!nodeHash.containsKey(c2)) // No est?.
				{
					idNodo2 = nodeCount++;
					nodeAux = new NodeGv(c2, idNodo2);
					nodeHash.put(c2, nodeAux);
					writeNode(nodeAux);
				} else {
					nodeAux = (NodeGv) nodeHash.get(c2);
				}
				idNodo2 = nodeAux.getId().intValue();

				if (typeFieldIndex != -1)
					valAux = (NumericValue) sds.getFieldValue(i, typeFieldIndex);
				else
					valAux = ValueFactory.createValue(0); // no hay tipo
				arcType = valAux.shortValue();
				// TipoTramo = DBFReadIntegerAttribute(hDBF, i, indiceCampo1);
				
				if (distFieldIndex != -1)
					valAux = (NumericValue) sds.getFieldValue(i, distFieldIndex);
				else
					valAux = ValueFactory.createValue(jtsGeom.getLength());
				distance = valAux.floatValue();
				// Distancia = (float) DBFReadDoubleAttribute(hDBF, i,
				// indiceCampo2);
				if (costFieldIndex != -1)
				{
					valAux = (NumericValue) sds.getFieldValue(i, costFieldIndex);
					cost = valAux.doubleValue();
				}
				else
					cost = distance;
				
				direction = -1;

				if (senseFieldIndex == -1)
					direction = 3; // 3-> Doble sentido, 1-> seg?n viene, 2 ->
				// al rev?s, cualquier otro valor-> No hay
				// arco
				else {
					valAux = (NumericValue) sds.getFieldValue(i,
							senseFieldIndex);
					direction = valAux.shortValue();
				}
				
				if (direction == 3) {
					sentidoDigit = 1; // En esa direcci?n
					writeEdge(i, sentidoDigit, idNodo1, idNodo2, arcType,
							distance, cost);
					edgeCount++;

					sentidoDigit = 0;
					writeEdge(i, sentidoDigit, idNodo2, idNodo1, arcType,
							distance, cost);
					edgeCount++;

				}
				if (direction == 1) {
					sentidoDigit = 1; // En esa direcci?n
					writeEdge(i, sentidoDigit, idNodo1, idNodo2, arcType,
							distance, cost);
					edgeCount++;
				}
				if (direction == 2) {
					sentidoDigit = 0;
					writeEdge(i, sentidoDigit, idNodo2, idNodo1, arcType,
							distance, cost);
					edgeCount++;

				}

			}

			nodeWriter.postProcess();
			edgeWriter.postProcess();
		} catch (com.hardcode.gdbms.engine.data.driver.DriverException e) {
			e.printStackTrace();
			throw new DriverException(e);
		} catch (DriverIOException e) {
			e.printStackTrace();
			throw new DriverException(e);
		}

		
	}

	private void writeEdge(int id, short sense, int idNodeOrig, int idNodeEnd,
			short tipoTramo, double dist, double cost) throws EditionException {
		Value[] values = new Value[edgeFields.length];
		values[0] = ValueFactory.createValue(id);
		values[1] = ValueFactory.createValue(sense);
		values[2] = ValueFactory.createValue(idNodeOrig);
		values[3] = ValueFactory.createValue(idNodeEnd);
		values[4] = ValueFactory.createValue(tipoTramo);
		values[5] = ValueFactory.createValue(dist);
		values[6] = ValueFactory.createValue(cost);
		DefaultRow myRow = new DefaultRow(values, id + "");
		IRowEdited editedRow = new DefaultRowEdited(myRow,
				DefaultRowEdited.STATUS_ADDED, id);

		edgeWriter.process(editedRow);
	}
	
	private void writeNode(NodeGv node) throws EditionException {
		Value[] values = new Value[nodeFields.length];
		int id = node.getId().intValue();
		values[0] = ValueFactory.createValue(id);
		values[1] = ValueFactory.createValue(node.getCoordinate().x);
		values[2] = ValueFactory.createValue(node.getCoordinate().y);
		DefaultRow myRow = new DefaultRow(values, id + "");
		IRowEdited editedRow = new DefaultRowEdited(myRow,
				DefaultRowEdited.STATUS_ADDED, id);

		nodeWriter.process(editedRow);	
	}

	public void setEdgeWriter(IWriter edgeWriter) {
		this.edgeWriter = edgeWriter;
	}

	public void setNodeWriter(IWriter nodeWriter) {
		this.nodeWriter = nodeWriter;
	}


}



