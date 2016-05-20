/*
 * Created on 01-feb-2006
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
 * $Id: BufferGeoprocess.java 33353 2010-09-02 11:20:30Z jpiera $
 * $Log$
 * Revision 1.6  2007-08-07 15:09:22  azabala
 * changes to remove UnitUtils' andami dependencies
 *
 * Revision 1.5  2007/05/15 07:24:19  cesar
 * Add the finished method for execution from Event Dispatch Thread
 *
 * Revision 1.4  2007/03/06 16:47:58  caballero
 * Exceptions
 *
 * Revision 1.3  2006/07/21 11:04:50  azabala
 * Unified createTask and process() methods
 *
 * Revision 1.2  2006/06/29 07:33:57  fjp
 * Cambios ISchemaManager y IFieldManager por terminar
 *
 * Revision 1.1  2006/06/20 18:20:45  azabala
 * first version in cvs
 *
 * Revision 1.2  2006/06/02 18:20:58  azabala
 * cuando el buffer es con dissolve se crea indice espacial para optimizar
 *
 * Revision 1.1  2006/05/24 21:14:41  azabala
 * primera version en cvs despues de refactoring orientado a crear un framework extensible de geoprocessing
 *
 * Revision 1.14  2006/05/01 19:18:09  azabala
 * revisi?n general del buffer (a?adidos anillos concentricos, buffers interiores y exteriores, etc)
 *
 * Revision 1.13  2006/03/23 21:02:58  azabala
 * *** empty log message ***
 *
 * Revision 1.12  2006/03/17 19:52:31  azabala
 * *** empty log message ***
 *
 * Revision 1.11  2006/03/14 19:35:13  azabala
 * *** empty log message ***
 *
 * Revision 1.10  2006/03/14 18:32:46  fjp
 * Cambio con LayerDefinition para que sea compatible con la definici?n de tablas tambi?n.
 *
 * Revision 1.9  2006/03/07 21:01:33  azabala
 * *** empty log message ***
 *
 * Revision 1.8  2006/03/06 19:48:39  azabala
 * *** empty log message ***
 *
 * Revision 1.7  2006/03/05 19:56:40  azabala
 * *** empty log message ***
 *
 * Revision 1.6  2006/02/17 16:04:50  azabala
 * *** empty log message ***
 *
 * Revision 1.5  2006/02/13 21:13:49  azabala
 * *** empty log message ***
 *
 * Revision 1.4  2006/02/13 18:38:03  azabala
 * *** empty log message ***
 *
 * Revision 1.3  2006/02/13 17:52:45  azabala
 * *** empty log message ***
 *
 * Revision 1.2  2006/02/12 21:02:44  azabala
 * *** empty log message ***
 *
 * Revision 1.1  2006/02/02 19:47:12  azabala
 * many refactorings
 *
 * Revision 1.1  2006/02/01 19:42:04  azabala
 * First version in CVS
 *
 *
 */
package com.iver.cit.gvsig.geoprocess.impl.buffer.fmap;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.cresques.cts.ICoordTrans;
import org.cresques.cts.IProjection;

import com.hardcode.gdbms.driver.exceptions.InitializeDriverException;
import com.hardcode.gdbms.driver.exceptions.OpenDriverException;
import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.driver.exceptions.SchemaEditionException;
import com.hardcode.gdbms.engine.data.driver.DriverException;
import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
import com.iver.cit.gvsig.exceptions.layers.LoadLayerException;
import com.iver.cit.gvsig.exceptions.visitors.ProcessVisitorException;
import com.iver.cit.gvsig.exceptions.visitors.VisitorException;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.drivers.ILayerDefinition;
import com.iver.cit.gvsig.fmap.drivers.SHPLayerDefinition;
import com.iver.cit.gvsig.fmap.drivers.shp.IndexedShpDriver;
import com.iver.cit.gvsig.fmap.edition.writers.shp.ShpWriter;
import com.iver.cit.gvsig.fmap.layers.FBitSet;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.LayerFactory;
import com.iver.cit.gvsig.fmap.layers.ReadableVectorial;
import com.iver.cit.gvsig.fmap.operations.strategies.Strategy;
import com.iver.cit.gvsig.fmap.operations.strategies.StrategyManager;
import com.iver.cit.gvsig.geoprocess.core.fmap.AbstractGeoprocess;
import com.iver.cit.gvsig.geoprocess.core.fmap.FeaturePersisterProcessor2;
import com.iver.cit.gvsig.geoprocess.core.fmap.GeometryPersisterProcessor;
import com.iver.cit.gvsig.geoprocess.core.fmap.GeoprocessException;
import com.iver.cit.gvsig.geoprocess.core.fmap.IOneLayerGeoprocess;
import com.iver.cit.gvsig.geoprocess.core.fmap.XTypes;
import com.iver.cit.gvsig.geoprocess.impl.dissolve.fmap.AdjacencyDissolveVisitor;
import com.iver.cit.gvsig.geoprocess.impl.dissolve.fmap.DissolveVisitor;
import com.iver.utiles.swing.threads.CancellableMonitorable;
import com.iver.utiles.swing.threads.DefaultCancellableMonitorable;
import com.iver.utiles.swing.threads.IMonitorableTask;

/**
 * Geoprocess that computes a buffer area around each feature's geometry of the
 * input layer. <br>
 * All the points interior to this buffer area has to be at a distance inferior
 * to "buffer distance" to the original geometry. This buffer distance could be
 * constant, or it could be a function of the value of a feature attribute.<br>
 *
 * @author azabala
 *
 */
public class BufferGeoprocess extends AbstractGeoprocess implements
		IOneLayerGeoprocess {

	/**
	 * flag that represents buffer computing with one only buffer distance.
	 */
	public static final byte CONSTANT_DISTANCE_STRATEGY = 0;

	/**
	 * buffer computing with variable buffer distance in function of feature
	 * attribute value
	 */
	public static final byte ATTRIBUTE_DISTANCE_STRATEGY = 1;


	/**
	 * Schema of the result layer
	 */
	private ILayerDefinition resultLayerDefinition;

	/**
	 * the geoprocess will operate only with selected elements of layer, or with
	 * all elements.<br>
	 * By default, it work with all elements.
	 */
	private boolean bufferOnlySelection = false;

	/**
	 * this flag makes the process to dissolve or not geometries of the result
	 * layer, to avoid intersections in a polygon layer. By default, is true.<br>
	 */
	private boolean dissolveBuffers = false;

	/**
	 * Iterates over input layer geometries to compute buffered geometries by
	 * using a distance buffer function.
	 */
	private BufferVisitor bufferVisitor;

	/**
	 * Constructor
	 *
	 * @param bufferLayer
	 *            layer to buffer
	 */
	public BufferGeoprocess(FLyrVect bufferLayer) {
		setFirstOperand(bufferLayer);
	}

	/**
	 * Sets layer to buffer
	 */
	public void setFirstOperand(FLyrVect bufferLayer) {
		this.firstLayer = bufferLayer;
	}

	/**
	 * Sets user entry params to this geoprocess. This params represents user
	 * preferences (FLayer are not optional params)
	 */
	public void setParameters(Map params) throws GeoprocessException {
		Boolean onlySelection = (Boolean) params.get("layer_selection");
		if (onlySelection != null)
			bufferOnlySelection = onlySelection.booleanValue();

		
		IProjection projection = (IProjection) params.get("projection");
		int distanceUnits = ((Integer)params.get("distanceunits")).intValue();
		int mapUnits = ((Integer)params.get("mapunits")).intValue();
		Byte strategy_flat = (Byte) params.get("strategy_flag");
		if (strategy_flat != null
				&& strategy_flat.byteValue() == ATTRIBUTE_DISTANCE_STRATEGY) {
			// must check attribute name not null
			String attributeName = (String) params.get("attr_name");
			if (attributeName != null) {
				bufferVisitor = new AttributeBufferVisitor(attributeName, projection, distanceUnits, mapUnits);
			} else {
				throw new GeoprocessException(
						"Buffer por atributo sin nombre de atributo");
			}
		} else {
			// default, constant strategy
			// must check distance not null
			Double dist = (Double) params.get("buffer_distance");
			if (dist != null) {
				double distance = dist.doubleValue();
				bufferVisitor = new ConstantDistanceBufferVisitor(distance, projection, distanceUnits, mapUnits);
			} else {
				throw new GeoprocessException(
						"Buffer por dist constante sin distancia");
			}
		}// else

		Boolean dissolve = (Boolean) params.get("dissolve_buffers");
		if (dissolve != null)
			dissolveBuffers = dissolve.booleanValue();
		bufferVisitor.setIsDissolve(dissolveBuffers);
		
		Byte capflag = (Byte) params.get("cap");
		if(capflag != null){
			bufferVisitor.setTypeOfCap(capflag.byteValue());
		}

		Byte typePolFlag = (Byte) params.get("typePolBuffer");
		if(typePolFlag != null){
			bufferVisitor.setTypeOfBuffer(typePolFlag.byteValue());
		}

		Integer numRingsFlag = (Integer) params.get("numRings");
		if(numRingsFlag != null){
			bufferVisitor.setNumberOfRadialBuffers(numRingsFlag.intValue());
		}
	}


	/**
	 * Checks preconditions to this geoprocess (input layer mustnt be null), etc
	 */
	public void checkPreconditions() throws GeoprocessException {
		if (firstLayer == null)
			throw new GeoprocessException("Buffer con capa de entrada a null");
		if (this.writer == null || this.schemaManager == null) {
			throw new GeoprocessException(
					"Operacion de buffer sin especificar capa de resultados");
		}
	}

	/**
	 * Computes buffer geoprocess, and saves results in solution layer.
	 *
	 */
	public void process() throws GeoprocessException {
		try {
			createTask().run();
		} catch (Exception e) {
			throw new GeoprocessException("Error durante la ejecuci?n del buffer", e);
		}
	}

	/**
	 * Creates a temp layer with the file referenced by writer.
	 * It is useful when you want to dissolve buffers, to avoid working
	 * with intermediate results in memory.
	 * @return
	 * @throws OpenDriverException
	 * @throws InitializeDriverException
	 * @throws LoadLayerException
	 * @throws IOException
	 * @throws DriverException
	 */
	public FLyrVect createTempLayer() throws OpenDriverException, InitializeDriverException, LoadLayerException {
		FLyrVect solution = null;
		String fileName = ((ShpWriter) writer).getShpPath();
		File file = new File(fileName);
		IProjection proj = firstLayer.getProjection();
		IndexedShpDriver driver = new IndexedShpDriver();
		driver.open(file);
		driver.initialize();
		solution = (FLyrVect) LayerFactory.createLayer(fileName, driver, file,
				proj);
		return solution;

	}



	/**
	 * Computes buffers of input layer, and saves them with the
	 * actual writer.
	 * @param strategy
	 * @param cancel
	 * @throws ReadDriverException
	 * @throws EditionException
	 * @throws DriverException
	 * @throws ProcessVisitorException
	 * @throws SchemaEditionException
	 * @throws ProcessWriterException
	 * @throws VisitorException
	 * @throws ExpansionFileReadException
	 */
	private void computeOnlyBuffers(Strategy strategy,
			CancellableMonitorable cancel) throws ReadDriverException, SchemaEditionException, ExpansionFileReadException, VisitorException {

		bufferVisitor.setBufferProcessor(new GeometryPersisterProcessor(
				this.resultLayerDefinition, this.schemaManager, this.writer));

		if (bufferOnlySelection) {
			strategy.process(bufferVisitor, firstLayer.getRecordset()
					.getSelection(), cancel);
		} else {
			strategy.process(bufferVisitor, cancel);
		}
	}

	/**
	 * Creates a LayerDefinition in function of:
	 * a) input layer
	 * b) user selections
	 * c) buffer geoprocess itselt operation
	 *
	 * Users of this geoprocess may construct a Writer from this LayerDefinition
	 * (for example, to save to shp ShpWriter needs a File and a LayerDefinition
	 * -to create this file with the proper schema-)
	 *
	 * FIXME
	 * We cant call this method before setParams(Map params). Launch a exception
	 *
	 * ILayerDefinition wont be the same for dissolved buffers layers
	 *
	 */
	public ILayerDefinition createLayerDefinition() {
		// In a buffer geoprocess we only well have a geometry
		// and a FID
		if (resultLayerDefinition == null) {
			if(!dissolveBuffers){
				resultLayerDefinition = new SHPLayerDefinition();
				try {
					resultLayerDefinition.setShapeType(FShape.POLYGON);
					resultLayerDefinition.setProjection(firstLayer.getProjection());
					resultLayerDefinition.setName(firstLayer.getName());
					resultLayerDefinition.setFieldsDesc(firstLayer.getRecordset().getFieldsDescription());
				} catch (ReadDriverException e) {
					e.printStackTrace();
				}
					return resultLayerDefinition;
			}else{
				if(bufferVisitor.getTypeOfBuffer() == BufferVisitor.BUFFER_INSIDE_OUTSIDE_POLY){
					resultLayerDefinition = createPositiveAndNegativeBufferDefinition();
				}else{
					resultLayerDefinition = createPositiveOrNegativeBufferDefinition();
				}
			}
		}
		return resultLayerDefinition;
	}

	/**
	 * Auxilar method to create a layer definition for buffer's process which
	 * needs to create only internal or external buffers.
	 * @return
	 */
	protected ILayerDefinition createPositiveOrNegativeBufferDefinition(){
		ILayerDefinition resultLayerDefinition = new SHPLayerDefinition();
		resultLayerDefinition.setShapeType(XTypes.POLYGON);
		FieldDescription[] fields = new FieldDescription[2];
		fields[0] = new FieldDescription();
		fields[0].setFieldLength(10);
		fields[0].setFieldName("FID");
		fields[0].setFieldType(XTypes.BIGINT);
		fields[1] = new FieldDescription();
		fields[1].setFieldLength(10);
		fields[1].setFieldDecimalCount(4);
		fields[1].setFieldName("DIST");
		fields[1].setFieldType(XTypes.DOUBLE);
		resultLayerDefinition.setFieldsDesc(fields);
		return resultLayerDefinition;
	}


	/**
	 * Auxilar method to create a layer definition for buffer's process which
	 * needs to create internal and external buffers.
	 * @return
	 */
	protected ILayerDefinition createPositiveAndNegativeBufferDefinition(){
		ILayerDefinition resultLayerDefinition = new SHPLayerDefinition();
		resultLayerDefinition.setShapeType(XTypes.POLYGON);
		FieldDescription[] fields = new FieldDescription[3];
		fields[0] = new FieldDescription();
		fields[0].setFieldLength(10);
		fields[0].setFieldName("FID");
		fields[0].setFieldType(XTypes.BIGINT);
		fields[1] = new FieldDescription();
		fields[1].setFieldLength(10);
		fields[1].setFieldDecimalCount(4);
		fields[1].setFieldName("FROM");
		fields[1].setFieldType(XTypes.DOUBLE);
		fields[2] = new FieldDescription();
		fields[2].setFieldLength(10);
		fields[2].setFieldDecimalCount(4);
		fields[2].setFieldName("TO");
		fields[2].setFieldType(XTypes.DOUBLE);

		resultLayerDefinition.setFieldsDesc(fields);
		return resultLayerDefinition;
	}

	/**
	 * Creates a CancelableMonitorable instance to monitor and cancels
	 * ITask's buffer creation.
	 * @return
	 */
	private DefaultCancellableMonitorable createCancelMonitor() {
		DefaultCancellableMonitorable monitor = new DefaultCancellableMonitorable();
		monitor.setInitialStep(0);
		if (this.bufferOnlySelection) {
			FBitSet selection = null;
			try {
				selection = firstLayer.getRecordset().getSelection();
			} catch (ReadDriverException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			int numSelected = selection.cardinality();
			monitor.setFinalStep(numSelected);
		} else {
			try {
				monitor.setFinalStep(firstLayer.getSource().getShapeCount());
			} catch (ReadDriverException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (!dissolveBuffers) {
			// if we dont look for adjacents, is a determinated
			// task (we know how many steps we are going to do
			monitor.setDeterminatedProcess(true);

		} else {
			monitor.setDeterminatedProcess(false);
		}// if dissolvebuffer
		return monitor;
	}

	public IMonitorableTask createTask() {
		/*
		 * Allows to monitor and cancel the task
		 */
		final CancellableMonitorable cancelMonitor = createCancelMonitor();
		/*
		 * Main Message for reporting buffer geoprocess
		 */
		final String message = PluginServices.getText(this, "Mensaje_buffer");
		/*
		 * Message for report individual step of buffer computation
		 */
		final String note = PluginServices.getText(this,
				"Mensaje_procesando_buffer");
		/*
		 * Message for report individual step of dissolving of buffers
		 */
		final String note2 = PluginServices.getText(this,
		"Mensaje_procesando_buffer2");
		/*
		 * Concatenation string
		 */
		final String of = PluginServices.getText(this, "De");

		return new IMonitorableTask() {
			DissolveVisitor dissolveVisitor;
			private boolean finished = false;
			String currentNote = note;

			public int getInitialStep() {
				return cancelMonitor.getInitialStep();
			}

			public int getFinishStep() {
				return cancelMonitor.getFinalStep();
			}

			public int getCurrentStep() {
				if(currentNote == note2)
					return dissolveVisitor.getDissolvedGeometries().cardinality();
				else
					return cancelMonitor.getCurrentStep();
			}

			public String getStatusMessage() {
				return message;
			}

			public String getNote() {
				return currentNote + ": " + getCurrentStep() + " " + of + " "
						+ getFinishStep();
			}

			public void cancel() {
				((DefaultCancellableMonitorable) cancelMonitor)
						.setCanceled(true);
				BufferGeoprocess.this.cancel();
			}

			public void run() {

				Strategy strategy = StrategyManager.getStrategy(firstLayer);
				resultLayerDefinition = createLayerDefinition();
				try {
					if (dissolveBuffers) {
						//computeAndDissolveBuffer(strategy, cancelMonitor);
						ShpWriter mainWriter = (ShpWriter) writer;
						writer = new ShpWriter();
						String temp = System.getProperty("java.io.tmpdir") +
								"/buffer" +
								System.currentTimeMillis() +
								".shp";
						File newFile = new File(temp);
						((ShpWriter) writer).setFile(newFile);
						((ShpWriter) writer).initialize(
								resultLayerDefinition);
						((SHPLayerDefinition) resultLayerDefinition).setFile(newFile);
						schemaManager.createSchema(resultLayerDefinition);
						bufferVisitor.setBufferProcessor(new GeometryPersisterProcessor(
								resultLayerDefinition, schemaManager, writer));
						computeOnlyBuffers(strategy, cancelMonitor);
						//dissolving buffers
						cancelMonitor.setCurrentStep(0);
						FLyrVect tempLayer = createTempLayer();
						tempLayer.createSpatialIndex();
						cancelMonitor.setFinalStep(tempLayer.getSource().getShapeCount());
						writer = mainWriter;
						FeaturePersisterProcessor2 processor = new FeaturePersisterProcessor2(
								writer);
//						AdjacencyDissolveVisitor dissolveVisitor =
//							new AdjacencyDissolveVisitor(null, processor);
						dissolveVisitor =
							createDissolveVisitor(processor);
						currentNote = note2;
						Strategy secondPassStrategy = StrategyManager.getStrategy(tempLayer);
						dissolveVisitor.setStrategy(secondPassStrategy);
						if(dissolveVisitor instanceof AdjacencyDissolveVisitor){
							((AdjacencyDissolveVisitor)dissolveVisitor).setCancelMonitor(cancelMonitor);
						}
//						secondPassStrategy.process(dissolveVisitor, cancelMonitor);
					     ReadableVectorial source = tempLayer.getSource();
						 ICoordTrans ct = tempLayer.getCoordTrans();
						
						 if(dissolveVisitor.start(tempLayer)){
							 source.start();
							 for (int i = 0; i < source.getShapeCount(); i++) {
									if(cancelMonitor.isCanceled()){
										source.stop();
										return;
									}
									if(dissolveVisitor.getDissolvedGeometries().get(i))
										continue;
									IGeometry geom = source.getShape(i);
								    if (ct != null) {
										geom.reProject(ct);
									}
								    dissolveVisitor.visit(geom, i);
							}//for
							source.stop();		
						 }
						 dissolveVisitor.stop(tempLayer);
					} else {
						computeOnlyBuffers(strategy, cancelMonitor);
					}
					finished = true;
				} catch (Exception e) {
					e.printStackTrace();
					finished = true;
				}
			}

			/**
			 * Creates a DissolveVisitor to dissolve computed buffers.
			 * If we only computed a ring buffer, dissolve
			 * criteria will be adjacency.
			 * If we computed many buffers, dissolve criteria will be
			 * adjacency and alphanumeric (value of FROM-TO or DISTANCE fields of the
			 * buffer geometry)
			 * @return
			 */
			private DissolveVisitor createDissolveVisitor(FeaturePersisterProcessor2 processor) {
				//int numBuffers = bufferVisitor.getNumberOfRadialBuffers();
				//if(numBuffers == 1){
					return new AdjacencyDissolveVisitor(null,processor);
				//}
				/*DOCUMENTAR ESTE CASO
				else{
					//Esto no funciona bien. Para dissolver buffers
					//con varios rings, da errores topol?gicos.

					//La ?nica soluci?n es calcular 1? la uni?n, luego
					//y luego los multirings

					if(bufferVisitor.getTypeOfBuffer() == BufferVisitor.BUFFER_INSIDE_OUTSIDE_POLY){
						return new DissolveVisitor("FROM", processor);
					}else{
						return new DissolveVisitor("DIST", processor);
					}
				}
				*/
			}

			public boolean isDefined() {
				return cancelMonitor.isDeterminatedProcess();
			}

			public boolean isCanceled() {
				return cancelMonitor.isCanceled();
			}

			public boolean isFinished() {
				return finished;
			}

			public void finished() {
				// TODO Auto-generated method stub
				
			}
		};
	}
}

