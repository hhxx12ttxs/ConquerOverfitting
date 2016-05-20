/*
 * Created on 10-abr-2006
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
* $Id: AddResultLayerTask.java 24093 2008-10-19 07:42:52Z azabala $
* $Log$
* Revision 1.7  2007-05-15 07:24:19  cesar
* Add the finished method for execution from Event Dispatch Thread
*
* Revision 1.6  2007/03/06 16:47:58  caballero
* Exceptions
*
* Revision 1.5  2006/10/20 14:29:07  azabala
* temporal bug fixed (problems when we add FLayers to another FLayers)
*
* Revision 1.4  2006/08/29 07:56:30  cesar
* Rename the *View* family of classes to *Window* (ie: SingletonView to SingletonWindow, ViewInfo to WindowInfo, etc)
*
* Revision 1.3  2006/06/12 19:16:17  azabala
* a?adida comprobaci?n de que la capa resultado tiene elementos antes de a?adirla
*
* Revision 1.2  2006/06/08 18:22:31  azabala
* Se a?ade chequeo de capas vac?as antes de a?adir result al TOC
*
* Revision 1.1  2006/05/24 21:13:09  azabala
* primera version en cvs despues de refactoring orientado a crear un framework extensible de geoprocessing
*
* Revision 1.1  2006/04/11 17:55:51  azabala
* primera version en cvs
*
*
*/
package com.iver.cit.gvsig.geoprocess.core.gui;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.layers.CancelationException;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.geoprocess.core.fmap.GeoprocessException;
import com.iver.cit.gvsig.geoprocess.core.fmap.IGeoprocess;
import com.iver.utiles.swing.threads.IMonitorableTask;

/**
 * Implementation of IMonitorableTask that add result layer
 * of a geoprocess to the TOC in a background thread
 * @author azabala
 *
 */
public class AddResultLayerTask implements IMonitorableTask {
	/**
	 * Layers of a view's TOC.
	 */
	FLayers layers;
	/**
	 * These are fictitial steps of adding layer to TOC task,
	 * to get satisfactory visual results.
	 */
	int initialStep = 1;
	int currentStep = 5;
	int lastStep = 10;
	/**
	 * Finalization flag of the task
	 */
	boolean finished = false;
	/**
	 * Geoprocess whose result layer we want to add to TOC
	 */
	IGeoprocess geoprocess;
	/**
	 * Constructor
	 * @param geoprocess
	 */
	public AddResultLayerTask(IGeoprocess geoprocess) {
		this.geoprocess = geoprocess;
	}
	/**
	 * Sets FLayers instance, to add new layer to it
	 * @param layers
	 */
	public void setLayers(FLayers layers){
		this.layers = layers;
	}

	public int getInitialStep() {
		return initialStep;
	}

	public int getFinishStep() {
		return lastStep;
	}

	public int getCurrentStep() {
		if (!finished)
			return 5;
		else
			return lastStep;
	}

	public String getStatusMessage() {
		return "Loading layer...";
	}

	public String getNote() {
		return "";
	}

	public boolean isDefined() {
		return true;
	}

	public void cancel() {
		finished = true;
	}

	private boolean checkToAdd(FLayer layer) throws GeoprocessException{
		try {
			if(layer instanceof FLyrVect){
				FLyrVect result = (FLyrVect) layer;
				if(result.getSource().getShapeCount() > 0){
					return true;
				}
				return false;
			}else if (layer instanceof FLayers){
				FLayers result = (FLayers) layer;
				int numLayers = result.getLayersCount();
				if( numLayers == 0)
					return false;
				for(int i = 0; i < numLayers; i++){
					FLayer lyrI = result.getLayer(i);
					if(lyrI instanceof FLyrVect){
						if(((FLyrVect)lyrI).getSource().getShapeCount() != 0)
							return true;
					}//if
				}//for
				return false;
			}else//TODO Verificar esto si queremos meter en geoprocessing capas raster
				return false;
		} catch (ReadDriverException e) {
			throw new GeoprocessException("Error al chequear la capa resultado antes de pasarla al TOC");
		}
	}

	public void run() throws GeoprocessException {
		try {
			FLayer result = geoprocess.getResult();
			if(checkToAdd(result)){
				if(result instanceof FLayers){//TODO PRUEBA
					FLayers resultLayers = (FLayers)result;
					/*
					 Checks if root FLayers has these FLayers. If not, adds childs layers.
					 If true, doesnt add nothing (patch to work with topology project and 
					 referencing module with auxiliar layers. TODO Re-study
					 * */
					if(layers.getLayer(resultLayers.getName()) == null){
						for(int i = 0; i < resultLayers.getLayersCount(); i++)
							layers.addLayer(resultLayers.getLayer(i));
					}
				}else
					layers.addLayer(result);
			}
			else{
				JOptionPane.showMessageDialog(
						(JComponent) PluginServices.
							getMDIManager().
							getActiveWindow(),
						PluginServices.
							getText(this, "Error_capa_vacia"),
							"Error",
						JOptionPane.ERROR_MESSAGE);
			}

		} catch (CancelationException e) {
			throw new GeoprocessException(
					"Error al a?adir el resultado de un geoproceso a flayers");
		} finally {
			finished = true;
		}
	}

	public boolean isCanceled() {
		return false;
	}

	public boolean isFinished() {
		return finished;
	}
	/* (non-Javadoc)
	 * @see com.iver.utiles.swing.threads.IMonitorableTask#finished()
	 */
	public void finished() {
		// TODO Auto-generated method stub
		
	}
}


