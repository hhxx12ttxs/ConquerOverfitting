/*
 * Created on 28-may-2007
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
* $Id: DBFeatureIteratorTest.java 18621 2008-02-08 07:59:54Z jdominguez $
* $Log$
* Revision 1.2  2007-09-20 08:08:29  jaume
* ReadExpansionFileException removed from this context
*
* Revision 1.1  2007/05/29 19:11:03  azabala
* *** empty log message ***
*
*
*/
package com.iver.cit.gvsig.fmap.featureiterators;

import java.awt.geom.Rectangle2D;

import junit.framework.TestCase;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.exceptions.layers.LoadLayerException;
import com.iver.cit.gvsig.fmap.drivers.IFeatureIterator;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;

public class DBFeatureIteratorTest extends TestCase {

//	test with mysql jdbc driver
	public void test2(){
		try {
			FLyrVect layer = FeatureIteratorTest.newJdbcLayer("muni");
			IFeatureIterator iterator = layer.getSource().getFeatureIterator();
			int numFeatures = 0;
			while(iterator.hasNext()){
				iterator.next();
				numFeatures++;
			}
			assert(layer.getSource().getShapeCount() == numFeatures);
			double xmin = 260000d;
			double xmax = 340000d;
			double ymin = 4098300d;
			double ymax = 4180500d;
			Rectangle2D rect = new Rectangle2D.Double(xmin, ymin,
					(xmax-xmin), (ymax-ymin));
			iterator = layer.getSource().getFeatureIterator(rect, null, FeatureIteratorTest.newProjection, true);
			numFeatures = 0;
			while(iterator.hasNext()){
				iterator.next();
				numFeatures++;
			}
			assert(layer.getSource().getShapeCount() > numFeatures);


		} catch (LoadLayerException e) {
			e.printStackTrace();
		} catch (ReadDriverException e) {
			e.printStackTrace();
		} catch(Exception e){
			//Si se trata de ejecutar el test sin tener MySQL
			e.printStackTrace();
		}

	}

}


