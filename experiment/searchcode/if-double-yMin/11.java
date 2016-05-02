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
* $Id: PerformanceFeatureIteratorTest.java 13904 2007-09-20 08:08:29Z jaume $
* $Log$
* Revision 1.6  2007-09-20 08:08:29  jaume
* ReadExpansionFileException removed from this context
*
* Revision 1.5  2007/06/29 13:07:01  jaume
* +PictureLineSymbol
*
* Revision 1.4  2007/06/07 10:20:38  azabala
* includes closeIterator
*
* Revision 1.3  2007/06/07 09:31:42  azabala
* *** empty log message ***
*
* Revision 1.2  2007/05/30 20:12:41  azabala
* fastIteration = true optimized.
*
* Revision 1.1  2007/05/29 19:11:03  azabala
* *** empty log message ***
*
*
*/
package com.iver.cit.gvsig.fmap.featureiterators;

import java.awt.geom.Rectangle2D;

import junit.framework.TestCase;

import org.cresques.cts.ICoordTrans;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
import com.iver.cit.gvsig.exceptions.layers.LoadLayerException;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.drivers.IFeatureIterator;
import com.iver.cit.gvsig.fmap.drivers.featureiterators.SpatialQueryFeatureIterator;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.spatialindex.ISpatialIndex;


/**
 * Tests to probe feature iteration methods.
 *
 * These test are not functional-test (performance).

 * @author azabala
 *
 */
public class PerformanceFeatureIteratorTest extends TestCase {

   static FLyrVect lyr;


	static{
			try {
				lyr = (FLyrVect) FeatureIteratorTest.newLayer("ejesc.shp",
										FeatureIteratorTest.SHP_DRIVER_NAME);
				lyr.setAvailable(true);
			} catch (LoadLayerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	public void test1() {
		try {
			//pruebas de iteracion espacial
			System.out.println("TEST 1: ESPACIAL CON FULL EXTENT Y REPROYECCI?N");
			Rectangle2D rect = lyr.getFullExtent();
			IFeature feature = null;
			//fast iteration
			long t0 = System.currentTimeMillis();
			ISpatialIndex spatialIndex = lyr.getSource().getSpatialIndex();
			lyr.getSource().setSpatialIndex(null);

			//Sin indice espacial, rapida
			//si pedimos reproyeccion, el rectangulo de consulta debe estar en la proyeccion
			//de destino
			ICoordTrans trans = FeatureIteratorTest.PROJECTION_DEFAULT.getCT(FeatureIteratorTest.newProjection);
			rect = trans.convert(rect);
			IFeatureIterator iterator = lyr.getSource().getFeatureIterator(rect,
															null,
															FeatureIteratorTest.newProjection,
															true);
			int numFeatures = 0;
			while(iterator.hasNext()){
				feature = iterator.next();
				numFeatures++;
			}
			long t1 = System.currentTimeMillis();
			iterator.closeIterator();
			//sin indice espacial, lenta
			iterator = lyr.getSource().getFeatureIterator(rect,
															null,
								FeatureIteratorTest.newProjection,
												false);
			int numFeatures2 = 0;
			while(iterator.hasNext()){
				feature = iterator.next();
				numFeatures2++;
			}
			long t2 = System.currentTimeMillis();
			iterator.closeIterator();
			lyr.getSource().setSpatialIndex(spatialIndex);
			long t3 = System.currentTimeMillis();

			//con indice espacial rapida
			iterator = lyr.getSource().getFeatureIterator(rect,
															null,
								FeatureIteratorTest.newProjection,
												true);
			int numFeatures3 = 0;
			while(iterator.hasNext()){
				feature = iterator.next();
				numFeatures3++;
			}
			long t4 = System.currentTimeMillis();
			iterator.closeIterator();
			//con indice espacial lenta
			iterator = lyr.getSource().getFeatureIterator(rect,
					null,
					FeatureIteratorTest.newProjection,
					false);
			int numFeatures4 = 0;
			while(iterator.hasNext()){
			feature = iterator.next();
			numFeatures4++;
			}
			long t5 = System.currentTimeMillis();
			iterator.closeIterator();

			System.out.println((t1-t0)+" en la iteracion rapida sin indice espacial");
			System.out.println("Recuperados "+numFeatures);
			System.out.println((t4-t3)+" en la iteracion rapida con indice espacial");
			System.out.println("Recuperados "+numFeatures3);
			System.out.println((t2-t1)+" en la iteracion lenta sin indice espacial");
			System.out.println("Recuperados "+numFeatures2);
			System.out.println((t5-t4)+" en la iteracion lenta con indice espacial");
			System.out.println("Recuperados "+numFeatures4);

		} catch (ReadDriverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}



	//test to ask a feature over the limit (numfeatures) to low level shapefile driver
	//classes
//	public void test2(){
//		try {
//			FLyrVect layer = (FLyrVect) FeatureIteratorTest.newLayer("poly-valencia.shp", FeatureIteratorTest.SHP_DRIVER_NAME);
//			int numShapes = layer.getSource().getShapeCount();
//			ReadableVectorial source = layer.getSource();
//			for(int i = numShapes -1; i < (numShapes + 50); i++){
//				source.getShape(i);
//			}
//			assertTrue(1 == 2);//si llega aqui, no pasa el test
//		} catch (LoadLayerException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ReadDriverException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ExpansionFileReadException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}

	//test to compare fast iteration based in spatial index with precisse iteration
	//with a little filter area
	public void test3(){
		double xmin = 724000;
		double xmax = 725000;
		double ymin = 4373800;
		double ymax = 4374300;
		System.out.println("TEST 2: ESPACIAL CON RECTANGULO PEQUE?O Y REPROYECCI?N");
		Rectangle2D rect = new Rectangle2D.Double(xmin, ymin, (xmax-xmin), (ymax-ymin));
		ICoordTrans trans = FeatureIteratorTest.PROJECTION_DEFAULT.
							getCT(FeatureIteratorTest.newProjection);
		//si pedimos reproyeccion, el rectangulo de consulta debe estar en la proyeccion
		//de destino
		rect = trans.convert(rect);


		IFeature feature = null;
		//fast iteration
		try {
			//fast iteration
			long t0 = System.currentTimeMillis();
			ISpatialIndex spatialIndex = lyr.getSource().getSpatialIndex();
			lyr.getSource().setSpatialIndex(null);

			//Sin indice espacial, rapida

			IFeatureIterator iterator = lyr.getSource().getFeatureIterator(rect,
															null,
															FeatureIteratorTest.newProjection,
															true);
			int numFeatures = 0;
			while(iterator.hasNext()){
				feature = iterator.next();
				numFeatures++;
			}
			long t1 = System.currentTimeMillis();
			iterator.closeIterator();
			//sin indice espacial, lenta
			iterator = lyr.getSource().getFeatureIterator(rect,
															null,
								FeatureIteratorTest.newProjection,
												false);
			int numFeatures2 = 0;
			while(iterator.hasNext()){
				feature = iterator.next();
				numFeatures2++;
			}
			long t2 = System.currentTimeMillis();
			iterator.closeIterator();
			lyr.getSource().setSpatialIndex(spatialIndex);
			long t3 = System.currentTimeMillis();

			//con indice espacial rapida
			iterator = lyr.getSource().getFeatureIterator(rect,
															null,
								FeatureIteratorTest.newProjection,
												true);
			int numFeatures3 = 0;
			while(iterator.hasNext()){
				feature = iterator.next();
				numFeatures3++;
			}
			long t4 = System.currentTimeMillis();
			iterator.closeIterator();
			//con indice espacial lenta
			iterator = lyr.getSource().getFeatureIterator(rect,
					null,
					FeatureIteratorTest.newProjection,
					false);
			int numFeatures4 = 0;
			while(iterator.hasNext()){
			feature = iterator.next();
			numFeatures4++;
			}
			long t5 = System.currentTimeMillis();
			iterator.closeIterator();

			System.out.println((t1-t0)+" en la iteracion rapida sin indice espacial");
			System.out.println("Recuperados "+numFeatures);
			System.out.println((t4-t3)+" en la iteracion rapida con indice espacial");
			System.out.println("Recuperados "+numFeatures3);
			System.out.println((t2-t1)+" en la iteracion lenta sin indice espacial");
			System.out.println("Recuperados "+numFeatures2);
			System.out.println((t5-t4)+" en la iteracion lenta con indice espacial");
			System.out.println("Recuperados "+numFeatures4);

		} catch (ExpansionFileReadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ReadDriverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	//the same test as test1 but without reprojection
	public void test4() {
		try {
			//pruebas de iteracion espacial
			Rectangle2D rect = lyr.getFullExtent();
			IFeature feature = null;
			System.out.println("TEST 3: ESPACIAL CON FULL EXTENT SIN REPROYECCI?N");
			//fast iteration
			long t0 = System.currentTimeMillis();
			ISpatialIndex spatialIndex = lyr.getSource().getSpatialIndex();
			lyr.getSource().setSpatialIndex(null);


			IFeatureIterator iterator = lyr.getSource().getFeatureIterator(rect,
															null,
															FeatureIteratorTest.PROJECTION_DEFAULT,
															true);
			int numFeatures = 0;
			while(iterator.hasNext()){
				feature = iterator.next();
				numFeatures++;
			}
			long t1 = System.currentTimeMillis();

			//sin indice espacial, lenta
			iterator = lyr.getSource().getFeatureIterator(rect,
															null,
													FeatureIteratorTest.PROJECTION_DEFAULT,
												false);
			int numFeatures2 = 0;
			while(iterator.hasNext()){
				feature = iterator.next();
				numFeatures2++;
			}
			long t2 = System.currentTimeMillis();

			lyr.getSource().setSpatialIndex(spatialIndex);
			long t3 = System.currentTimeMillis();

			//con indice espacial rapida
			iterator = lyr.getSource().getFeatureIterator(rect,
															null,
													FeatureIteratorTest.PROJECTION_DEFAULT,
												true);
			int numFeatures3 = 0;
			while(iterator.hasNext()){
				feature = iterator.next();
				numFeatures3++;
			}
			long t4 = System.currentTimeMillis();
			//con indice espacial lenta
			iterator = lyr.getSource().getFeatureIterator(rect,
					null,
					FeatureIteratorTest.PROJECTION_DEFAULT,
					false);
			int numFeatures4 = 0;
			while(iterator.hasNext()){
			feature = iterator.next();
			numFeatures4++;
			}
			long t5 = System.currentTimeMillis();


			System.out.println((t1-t0)+" en la iteracion rapida sin indice espacial");
			System.out.println("Recuperados "+numFeatures);
			System.out.println((t4-t3)+" en la iteracion rapida con indice espacial");
			System.out.println("Recuperados "+numFeatures3);
			System.out.println((t2-t1)+" en la iteracion lenta sin indice espacial");
			System.out.println("Recuperados "+numFeatures2);
			System.out.println((t5-t4)+" en la iteracion lenta con indice espacial");
			System.out.println("Recuperados "+numFeatures4);

		} catch (ReadDriverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	//the same test as test3 without reprojection
	public void test5(){
		double xmin = 724000;
		double xmax = 725000;
		double ymin = 4373800;
		double ymax = 4374300;
		Rectangle2D rect = new Rectangle2D.Double(xmin, ymin, (xmax-xmin), (ymax-ymin));
		System.out.println("TEST 4: ESPACIAL CON RECTANGULO PEQUE?O SIN REPROYECCI?N");
		IFeature feature = null;
		//fast iteration
		try {
			//fast iteration
			long t0 = System.currentTimeMillis();
			ISpatialIndex spatialIndex = lyr.getSource().getSpatialIndex();
			lyr.getSource().setSpatialIndex(null);

			//Sin indice espacial, rapida

			IFeatureIterator iterator = lyr.getSource().getFeatureIterator(rect,
															null,
															FeatureIteratorTest.PROJECTION_DEFAULT,
															true);
			int numFeatures = 0;
			while(iterator.hasNext()){
				feature = iterator.next();
				numFeatures++;
			}
			long t1 = System.currentTimeMillis();

			//sin indice espacial, lenta
			iterator = lyr.getSource().getFeatureIterator(rect,
															null,
														FeatureIteratorTest.PROJECTION_DEFAULT,
												false);
			int numFeatures2 = 0;
			while(iterator.hasNext()){
				feature = iterator.next();
				numFeatures2++;
			}
			long t2 = System.currentTimeMillis();

			lyr.getSource().setSpatialIndex(spatialIndex);
			long t3 = System.currentTimeMillis();

			//con indice espacial rapida
			iterator = lyr.getSource().getFeatureIterator(rect,
															null,
														FeatureIteratorTest.PROJECTION_DEFAULT,
												true);
			int numFeatures3 = 0;
			while(iterator.hasNext()){
				feature = iterator.next();
				numFeatures3++;
			}
			long t4 = System.currentTimeMillis();
			//con indice espacial lenta
			iterator = lyr.getSource().getFeatureIterator(rect,
					null,
					FeatureIteratorTest.PROJECTION_DEFAULT,
					false);
			int numFeatures4 = 0;
			while(iterator.hasNext()){
			feature = iterator.next();
			numFeatures4++;
			}
			long t5 = System.currentTimeMillis();


			System.out.println((t1-t0)+" en la iteracion rapida sin indice espacial");
			System.out.println("Recuperados "+numFeatures);
			System.out.println((t4-t3)+" en la iteracion rapida con indice espacial");
			System.out.println("Recuperados "+numFeatures3);
			System.out.println((t2-t1)+" en la iteracion lenta sin indice espacial");
			System.out.println("Recuperados "+numFeatures2);
			System.out.println((t5-t4)+" en la iteracion lenta con indice espacial");
			System.out.println("Recuperados "+numFeatures4);

		} catch (ExpansionFileReadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ReadDriverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 Este test hay que refinarlo. Simplemente es un intento, mediante simulacion, de encontrar el valor ideal
	 de rectangulo de consulta para discernir cuando una iteraci?n debe hacer uso de la caracteristica boundedshapes
	 (leer rectangulos sin leer la geometria, a modo de 'indice espacial') y cuando no
	*/
	public void test6(){
		double xmin = 724000;
		double width = 1000;
		double ymin = 4373800;
		double height = 500;
		System.out.println("TEST 5: BUSQUEDA DEL LIMITE OPTIMO ENTRE BOUNDEDSHAPES Y PRECISSE PARA ITERACIONES R?PIDAS");
		//fast iteration
		try {
			//fast iteration
			lyr.getSource().setSpatialIndex(null);


			double BOUND_FACTOR = SpatialQueryFeatureIterator.BOUNDED_SHAPES_FACTOR;
			double lyrWidth = lyr.getSource().getFullExtent().getWidth();
			Rectangle2D rect = new Rectangle2D.Double(xmin, ymin, width, height);
			while(width <= lyrWidth){

				ICoordTrans trans = FeatureIteratorTest.PROJECTION_DEFAULT.
									getCT(FeatureIteratorTest.newProjection);
				//si pedimos reproyeccion, el rectangulo de consulta debe estar en la proyeccion
				//de destino
				rect = trans.convert(rect);
//				SpatialQueryFeatureIterator.BOUNDED_SHAPES_FACTOR = 4d;
				BOUND_FACTOR = SpatialQueryFeatureIterator.BOUNDED_SHAPES_FACTOR;
				while (BOUND_FACTOR >= 1){
					long t0 = System.currentTimeMillis();
					IFeatureIterator iterator = lyr.getSource().getFeatureIterator(rect,
																	null,
																	FeatureIteratorTest.newProjection,
																	true);
					while(iterator.hasNext()){
						iterator.next();
					}
					long t1 = System.currentTimeMillis();


					Rectangle2D driverExtent = lyr.getSource().getFullExtent();
					double areaExtent = rect.getWidth() * rect.getHeight();
					double areaFullExtent = driverExtent.getWidth() *
						                         driverExtent.getHeight();
					System.out.println("areaExtent="+areaExtent+", areaFullExtent="+areaFullExtent);
					System.out.println("full/BoundFactor="+(areaFullExtent / BOUND_FACTOR));
					System.out.println("BOUND_F="+BOUND_FACTOR+";time="+(t1-t0));
					BOUND_FACTOR /= 2d;
//					SpatialQueryFeatureIterator.BOUNDED_SHAPES_FACTOR = BOUND_FACTOR;
				}//while
				width *= 3;
				height *= 3;
				rect = new Rectangle2D.Double(xmin, ymin, width, height);
			}//while

		} catch (ExpansionFileReadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ReadDriverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}







