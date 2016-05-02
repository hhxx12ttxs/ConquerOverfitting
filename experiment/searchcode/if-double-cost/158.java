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
package org.gvsig.graph.core.writers;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.imageio.stream.FileImageOutputStream;

import org.cresques.cts.ICoordTrans;
import org.gvsig.exceptions.BaseException;
import org.gvsig.graph.core.GraphException;
import org.gvsig.graph.core.NodeGv;

import com.hardcode.gdbms.engine.instruction.IncompatibleTypesException;
import com.hardcode.gdbms.engine.values.BooleanValue;
import com.hardcode.gdbms.engine.values.NumericValue;
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.drivers.DriverAttributes;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
import com.iver.cit.gvsig.fmap.layers.VectorialAdapter;
import com.iver.cit.gvsig.util.SnappingCoordinateMap;
import com.iver.utiles.swing.threads.CancellableMonitorable;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @author fjp
 * 
 * 
 */
public class NetworkFileRedWriter extends AbstractNetworkWriter {
	File redFile;
	CancellableMonitorable cancel;
	private double snapTolerance = 0d;
	private double unitsFactor = 1.0; // 1.0->secs (no conversion). 60 ->
										// minutes, 3600 hours, etc.
	private String digitalizationDirection;
	private String reverseDigitalizationDirection;

	public NetworkFileRedWriter() {

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gvsig.graph.core.INetworkWriter#writeNetwork(boolean)
	 */
	public void writeNetwork() throws BaseException {
		// PRIMERO VAN EL NUMERO DE TRAMOS, LUEGO EL NUMERO DE ARCOS Y LUEGO
		// EL NUMERO DE NODOS, DESPUES
		// IDTRAMO-SENTIDO_DIGITALIZACION-IDNODOORIGEN-IDNODODESTINO-TIPOTRAMO-DISTANCIA
		// Y POR FIN
		// IDNODO-Xdouble-Ydouble

		// El campo sentido indica //3-> Doble sentido, 1-> seg?n viene, 2
		// -> al rev?s, cualquier otro valor-> No hay arco
		// TipoTamo va a ser un campo num?rico, entero.
		// 0-> Autopista.
		// 1-> Autov?a.
		// 2-> Nacional.
		// 3-> Nacional - Comarcal.
		// 4-> Comarcal.
		// 5-> Otras.
		// 6-> Ferry.
		// 7-> Conexiones
		// Los nombres son orientativos. Basta saber que puede haber hasta X
		// tipos distintos de tramos, cada uno
		// con su velocidad. Esa velocidad se fijar? en la funci?n
		// FijaVelocidades. OJO, empezar siempre desde el 0

		double distance, cost;
		short arcType;
		int direction;
		int i;
		int idNodo1, idNodo2, nodeCount, edgeCount;
		short sentidoDigit; // => 1 en esa direcci?n. 0=> Al contrario. SOLO
		// SE UTILIZA PARA LOS CALCULOS POR IDTRAMO Y
		// PORCENTAJE
		// PARA SABER SI EST? M?S CERCA DE UN NODO O DEL OTRO.
		Hashtable nodeHash = null;
		if (snapTolerance != 0d)
			nodeHash = new SnappingCoordinateMap(snapTolerance);
		else
			nodeHash = new Hashtable();
		ArrayList nodes = new ArrayList();

		try {
			// if (lyr.getShapeType() != FShape.LINE) {
			if ((lyr.getShapeType() & FShape.LINE) != FShape.LINE) {
				return;
			}
			RandomAccessFile file = new RandomAccessFile(redFile.getPath(),
					"rw");
			FileImageOutputStream output = new FileImageOutputStream(file);
			// FileChannel channel = file.getChannel();
			// MappedByteBuffer buf = channel.map(MapMode.READ_WRITE, 0,
			// 16 * 1024);
			// buf.order(ByteOrder.LITTLE_ENDIAN);
			output.setByteOrder(ByteOrder.LITTLE_ENDIAN);

			nodeCount = 0;
			VectorialAdapter adapter = (VectorialAdapter) lyr.getSource();
			adapter.start();
			int numTramos;

			ICoordTrans ct = lyr.getCoordTrans();

			numTramos = adapter.getShapeCount();
			// Cambiamos otra vez: Escribimos primero el n? de tramos. Luego va
			// el n? de arcos y el de nodos.
			// buf.putInt(numTramos);
			output.writeInt(numTramos);
			// /////// Cambiamos el formato: primero van los arcos y luego los
			// nodos
			// file.writeInt(0);
			output.writeInt(0);
			// OJO: El numero de arcos todav?a no lo sabemos, habr? que volver
			// luego y escribir el numero correcto. Es por lo de los sentidos.
			// Metemos 2 arcos si es doble sentido, y uno si es de un solo
			// sentido

			// Guardamos un long. Luego volveremos aqu? y grabaremos el n? de
			// nodos
			// file.writeInt(0);
			output.writeInt(numTramos);
			// AHORA METEMOS LOS NOMBRES DE LOS CAMPOS. EN EL CARGA RED LOS
			// USAREMOS PARA LEER DEL DBF.

			edgeCount = 0;

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

			DriverAttributes attr = adapter.getDriverAttributes();
			boolean bMustClone = false;
			if (attr != null) {
				if (attr.isLoadedInMemory()) {
					bMustClone = attr.isLoadedInMemory();
				}
			}

			NumericValue valAux = null;
			for (i = 0; i < numTramos; i++) {
				IGeometry geom = adapter.getShape(i);
				if (geom == null)
					continue;
				if (ct != null) {
					if (bMustClone)
						geom = geom.cloneGeometry();
					geom.reProject(ct);

				}
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
					nodes.add(nodeAux);
				} else {
					nodeAux = (NodeGv) nodeHash.get(c1);
				}
				idNodo1 = nodeAux.getId().intValue();

				if (!nodeHash.containsKey(c2)) // No est?.
				{
					idNodo2 = nodeCount++;
					nodeAux = new NodeGv(c2, idNodo2);
					nodeHash.put(c2, nodeAux);
					nodes.add(nodeAux);

				} else {
					nodeAux = (NodeGv) nodeHash.get(c2);
				}
				idNodo2 = nodeAux.getId().intValue();

				if (typeFieldIndex != -1)
					valAux = (NumericValue) sds
							.getFieldValue(i, typeFieldIndex);
				else
					valAux = ValueFactory.createValue(0); // no hay tipo
				arcType = valAux.shortValue();
				// TipoTramo = DBFReadIntegerAttribute(hDBF, i, indiceCampo1);

				if (distFieldIndex != -1)
					valAux = (NumericValue) sds
							.getFieldValue(i, distFieldIndex);
				else
					valAux = ValueFactory.createValue(jtsGeom.getLength());
				distance = valAux.floatValue();
				// Distancia = (float) DBFReadDoubleAttribute(hDBF, i,
				// indiceCampo2);
				if (costFieldIndex != -1) {
					valAux = (NumericValue) sds
							.getFieldValue(i, costFieldIndex);
					cost = valAux.doubleValue() * unitsFactor;
				} else
					cost = distance;

				direction = -1;

				if (senseFieldIndex == -1)
					direction = 3; // 3-> Doble sentido, 1-> seg?n viene, 2 ->
				// al rev?s
				else {
					Value aux = sds.getFieldValue(i, senseFieldIndex);
					String auxStr = aux.toString();
					direction = 3; // default
					if (auxStr.equals(digitalizationDirection))
						direction = 1;
					if (auxStr.equals(reverseDigitalizationDirection))
						direction = 2;

				}

				if (direction == 3) {
					sentidoDigit = 1; // En esa direcci?n
					writeEdge(output, i, sentidoDigit, idNodo1, idNodo2,
							arcType, distance, cost);
					edgeCount++;

					sentidoDigit = 0;
					writeEdge(output, i, sentidoDigit, idNodo2, idNodo1,
							arcType, distance, cost);
					edgeCount++;

				}
				if (direction == 1) {
					sentidoDigit = 1; // En esa direcci?n
					writeEdge(output, i, sentidoDigit, idNodo1, idNodo2,
							arcType, distance, cost);
					edgeCount++;
				}
				if (direction == 2) {
					sentidoDigit = 0;
					writeEdge(output, i, sentidoDigit, idNodo2, idNodo1,
							arcType, distance, cost);
					edgeCount++;

				}

				if (cancel != null) {
					cancel.reportStep();

				}
			}// for

			for (int j = 0; j < nodes.size(); j++) {
				NodeGv node = (NodeGv) nodes.get(j);
				output.writeInt(node.getId().intValue());
				output.writeDouble(node.getCoordinate().x);
				output.writeDouble(node.getCoordinate().y);
			}

			output.seek(0);
			// buf.position(0);
			output.writeInt(numTramos);
			output.writeInt(edgeCount);
			output.writeInt(nodes.size());

			// buf.force();
			output.close();
			file.close();

			adapter.stop();

			cancel.setCurrentStep(cancel.getFinalStep());

			return;
		} catch (IOException e) {
			e.printStackTrace();
			throw new GraphException(e);
		}
		// } catch (ReadDriverException e) {
		// e.printStackTrace();
		// throw new IOException(e);
		// } catch (ExpansionFileReadException e) {
		// e.printStackTrace();
		// throw new IOException(e);
		// }
	}

	public void setCancellableMonitorable(CancellableMonitorable cancel) {
		this.cancel = cancel;

	}

	public void setRedFile(File redFile) {
		this.redFile = redFile;

	}

	private void writeEdge(FileImageOutputStream output, int id, short sense,
			int idNodeOrig, int idNodeEnd, short tipoTramo, double dist,
			double cost) throws IOException {
		output.writeInt(id);
		output.writeInt(sense);

		output.writeInt(idNodeOrig);
		output.writeInt(idNodeEnd);
		output.writeInt(tipoTramo);
		output.writeDouble(dist);
		output.writeDouble(cost);

	}

	public void setSnapTolerance(double snapTolerance) {
		this.snapTolerance = snapTolerance;
	}

	public double getUnitsFactor() {
		return unitsFactor;
	}

	public void setUnitsFactor(double unitsFactor) {
		this.unitsFactor = unitsFactor;
	}

	public String getDigitalizationDirection() {
		return digitalizationDirection;
	}

	public void setDigitalizationDirection(String digitalizationDirection) {
		this.digitalizationDirection = digitalizationDirection;
	}

	public String getReverseDigitalizationDirection() {
		return reverseDigitalizationDirection;
	}

	public void setReverseDigitalizationDirection(
			String reverseDigitalizationDirection) {
		this.reverseDigitalizationDirection = reverseDigitalizationDirection;
	}

}
