/*
 * Created on 27-jun-2006
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
* $Id: XYShiftGeoprocess.java 13111 2007-08-16 14:41:36Z azabala $
* $Log$
* Revision 1.5  2007-08-16 14:41:36  azabala
* changes to remove UnitUtils' andami dependencies
*
* Revision 1.4  2007/05/15 07:23:26  cesar
* Add the finished method for execution from Event Dispatch Thread
*
* Revision 1.3  2007/03/06 16:48:14  caballero
* Exceptions
*
* Revision 1.2  2006/06/29 17:58:31  azabala
* *** empty log message ***
*
* Revision 1.1  2006/06/28 18:17:21  azabala
* first version in cvs
*
*
*/
package com.iver.cit.gvsig.geoprocess.impl.xyshift.fmap;

import java.util.Map;

import org.cresques.cts.IProjection;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.drivers.ILayerDefinition;
import com.iver.cit.gvsig.fmap.layers.FBitSet;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.operations.strategies.Strategy;
import com.iver.cit.gvsig.fmap.operations.strategies.StrategyManager;
import com.iver.cit.gvsig.geoprocess.core.fmap.AbstractGeoprocess;
import com.iver.cit.gvsig.geoprocess.core.fmap.DefinitionUtils;
import com.iver.cit.gvsig.geoprocess.core.fmap.FeaturePersisterProcessor2;
import com.iver.cit.gvsig.geoprocess.core.fmap.GeoprocessException;
import com.iver.cit.gvsig.geoprocess.core.util.UnitUtils;
import com.iver.utiles.swing.threads.AbstractMonitorableTask;
import com.iver.utiles.swing.threads.DefaultCancellableMonitorable;
import com.iver.utiles.swing.threads.IMonitorableTask;
/**
 * This geoprocess apply an offset in x and y directions producing
 * a new FLyrVect whose coordinates has been shifted.
 * @author azabala
 *
 */
public class XYShiftGeoprocess  extends AbstractGeoprocess {

	/**
	 * Schema of the result layer
	 */

	private ILayerDefinition resultLayerDefinition;

	/**
	 * flag to only clip selection of input layer
	 */
	private boolean onlyInputLayerSelection = false;

	/**
	 * feature visitor to apply an xy shift
	 */
	private XYShifterFeatureVisitor visitor;

	/**
	 * Processes shifted features (writing them)
	 */
	FeaturePersisterProcessor2 processor;

	/**
	 * x offset
	 */
	private double offsetX;
	/**
	 * y offset
	 */
	private double offsetY;


	public XYShiftGeoprocess(FLyrVect inputLayer){
		setFirstOperand(inputLayer);
	}


	public void setParameters(Map params) throws GeoprocessException {
		Boolean firstLayerSelection = (Boolean)
			params.get("firstlayerselection");
		if (firstLayerSelection != null)
			this.onlyInputLayerSelection =
				firstLayerSelection.booleanValue();
		
		IProjection projection = (IProjection) params.get("projection");
		int distanceUnits = ((Integer)params.get("distanceunits")).intValue();
		int mapUnits = ((Integer)params.get("mapunits")).intValue();
		
		Double xShift = (Double)
			params.get("xshift");
		if(xShift != null)
			this.offsetX = UnitUtils.
				getInInternalUnits(xShift.doubleValue(), projection, distanceUnits, mapUnits);
		Double yShift = (Double)
			params.get("yshift");
		if(yShift != null)
			this.offsetY = UnitUtils.
				getInInternalUnits(yShift.doubleValue(), projection, distanceUnits, mapUnits);
	}

	public void checkPreconditions() throws GeoprocessException {
		//TODO llevar a un  metodo verifyWriter de AbstractGeoprocess
		if (this.writer == null || this.schemaManager == null) {
			throw new GeoprocessException(
					"Operacion de xyshift sin especificar capa de resultados");
		}
		if(offsetX == 0 && offsetY == 0)
			throw new GeoprocessException("Geoproceso XYShift inicializado con un offset de 0,0");

	}

	public void process() throws GeoprocessException {
		try {
			new XYShiftTask().run();
		} catch (Exception e) {
			throw new GeoprocessException("Error al ejecutar el geoproceso XYShift", e);
		}

	}

	/**
	 * ITask (cancelable and progres-monitorable task) which makes all
	 * xyshift computations
	 * @author azabala
	 *
	 */
	class XYShiftTask extends AbstractMonitorableTask{

		private XYShiftTask(){
			setInitialStep(0);
			try {
				if(onlyInputLayerSelection){
					int numSelected = firstLayer.
										getRecordset().
										getSelection().
										cardinality();
					setFinalStep(numSelected);
				}else{
					int numShapes = firstLayer.getSource().getShapeCount();
					setFinalStep(numShapes);
				}//else
			} catch (ReadDriverException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			setDeterminatedProcess(true);
			setStatusMessage(PluginServices.getText(this, "XYShift._Progress_Message"));

		}

		public void run() throws Exception {
			processor =
				new FeaturePersisterProcessor2(writer);
			visitor = new XYShifterFeatureVisitor(processor,
					createLayerDefinition(), offsetX, offsetY );
			if(onlyInputLayerSelection)
				visitor.setSelection(firstLayer.getRecordset().getSelection());
			Strategy strategy = StrategyManager.getStrategy(firstLayer);
			try {
				//AbstractMonitorableTask is a cancel monitor too
				strategy.process(visitor, this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		//TODO INTERNACIONALIZAR LOS MENSAJES
		public String getNote() {
			return "Desplazando features..."  +
			" " +
			getCurrentStep()+
			" "+
			"de"+
			" "+
			getFinishStep();
		}

		public void cancel() {
			setCanceled(true);
			XYShiftGeoprocess.this.cancel();
		}

		/* (non-Javadoc)
		 * @see com.iver.utiles.swing.threads.IMonitorableTask#finished()
		 */
		public void finished() {
			// TODO Auto-generated method stub
			
		}
	}


	public IMonitorableTask createTask() {
		return new XYShiftTask();
	}

	public void setFirstOperand(FLyrVect firstLayer) {
		super.firstLayer = firstLayer;
	}


	public ILayerDefinition createLayerDefinition() {
		if(resultLayerDefinition == null){
			try {
				resultLayerDefinition = DefinitionUtils.
					createLayerDefinition(super.firstLayer);
			} catch (ReadDriverException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return resultLayerDefinition;
	}

	private DefaultCancellableMonitorable createCancelMonitor() {
		DefaultCancellableMonitorable monitor = new DefaultCancellableMonitorable();
		monitor.setInitialStep(0);
		monitor.setDeterminatedProcess(true);
		int numSteps = 0;
		try {
			if (onlyInputLayerSelection){
				FBitSet selection = firstLayer.getRecordset().getSelection();
				numSteps = selection.cardinality();
			}else{
					numSteps = firstLayer.getSource().getShapeCount();
			}
			monitor.setFinalStep(numSteps);
		} catch (ReadDriverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return monitor;
	}


}


