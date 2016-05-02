/*
 * Created on 17-abr-2007
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
* $Id: FeatureIteratorTest.java 18621 2008-02-08 07:59:54Z jdominguez $
* $Log$
* Revision 1.11  2007-09-20 08:08:29  jaume
* ReadExpansionFileException removed from this context
*
* Revision 1.10  2007/06/29 13:07:01  jaume
* +PictureLineSymbol
*
* Revision 1.9  2007/06/27 20:18:35  azabala
* *** empty log message ***
*
* Revision 1.8  2007/06/07 10:20:38  azabala
* includes closeIterator
*
* Revision 1.7  2007/06/07 09:31:42  azabala
* *** empty log message ***
*
* Revision 1.5  2007/05/29 19:11:03  azabala
* *** empty log message ***
*
* Revision 1.4  2007/05/23 16:54:29  azabala
* new test for bug 2510 phpcollab task (infinite loop for poly-valencia.shp)
*
* Revision 1.3  2007/05/08 08:48:27  jaume
* GRAPHIC TESTING FOR ISYMBOLS!!!
*
* Revision 1.2  2007/04/25 15:16:10  azabala
* tests for featureiterators with mysql database
*
* Revision 1.1  2007/04/19 18:12:56  azabala
* *** empty log message ***
*
*
*/
package com.iver.cit.gvsig.fmap.featureiterators;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.prefs.Preferences;

import junit.framework.TestCase;

import org.cresques.cts.IProjection;
import org.geotools.resources.geometry.XRectangle2D;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.exceptions.layers.LoadLayerException;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.ViewPort;
import com.iver.cit.gvsig.fmap.core.ICanReproject;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.v02.FConverter;
import com.iver.cit.gvsig.fmap.crs.CRSFactory;
import com.iver.cit.gvsig.fmap.drivers.ConnectionJDBC;
import com.iver.cit.gvsig.fmap.drivers.DBLayerDefinition;
import com.iver.cit.gvsig.fmap.drivers.DefaultJDBCDriver;
import com.iver.cit.gvsig.fmap.drivers.IFeatureIterator;
import com.iver.cit.gvsig.fmap.drivers.IVectorialJDBCDriver;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.LayerFactory;
import com.iver.utiles.swing.threads.Cancellable;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class FeatureIteratorTest extends TestCase {
	static final String fwAndamiDriverPath = "../_fwAndami/gvSIG/extensiones/com.iver.cit.gvsig/drivers";
	private static File baseDataPath;
	private static File baseDriversPath;

	public static String SHP_DRIVER_NAME = "gvSIG shp driver";
	public  static String DXF_DRIVER_NAME = "gvSIG DXF Memory Driver";

	//TODO MOVER TODO LO ESTATICO A UNA CLASE AUXILIAR QUE NO SEA JUNIT
	static IProjection PROJECTION_DEFAULT =
		CRSFactory.getCRS("EPSG:23030");
	static IProjection newProjection =
		CRSFactory.getCRS("EPSG:23029");

	static{
		try {
			doSetup();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void doSetup() throws Exception{
		URL url = FeatureIteratorTest.class.getResource("testdata");
		if (url == null)
			throw new Exception("No se encuentra el directorio con datos de prueba");

		baseDataPath = new File(url.getFile());
		if (!baseDataPath.exists())
			throw new Exception("No se encuentra el directorio con datos de prueba");

		baseDriversPath = new File(fwAndamiDriverPath);
		if (!baseDriversPath.exists())
			throw new Exception("Can't find drivers path: " + fwAndamiDriverPath);

		LayerFactory.setDriversPath(baseDriversPath.getAbsolutePath());
		if (LayerFactory.getDM().getDriverNames().length < 1)
			throw new Exception("Can't find drivers in path: " + fwAndamiDriverPath);
	}


	protected void setUp() throws Exception {
		super.setUp();
		doSetup();

	}


	public static  FLayer newLayer(String fileName,
									   String driverName)
								throws LoadLayerException {
		File file = new File(baseDataPath, fileName);
		return LayerFactory.createLayer(fileName,
										driverName,
										file, PROJECTION_DEFAULT);
	}

	public static FLyrVect newJdbcLayer(String layerName) throws LoadLayerException{
	        String dbURL = "jdbc:mysql://localhost:3306/datos";
	        String user = "root";
	        String pwd = "root";
	        String tableName = layerName;
	        //Connection conn;
	        try {
				Class.forName("com.mysql.jdbc.Driver");
			} catch (ClassNotFoundException e1) {
				throw new LoadLayerException(layerName, e1);
			}
			try {
				
				ConnectionJDBC conn = new ConnectionJDBC();
				conn.setDataConnection(dbURL, user, pwd);
				
		        String fidField = "gid";
		        String geomField = "geom";
		        String[] fields = new String[1];
		        fields[0] = "gid";
		        String whereClause = "";
		        IVectorialJDBCDriver driver = (IVectorialJDBCDriver)
		        	LayerFactory.getDM().getDriver("mySQL JDBC Driver");
		        String strEPSG = "23030";
		        DBLayerDefinition lyrDef = new DBLayerDefinition();
		        lyrDef.setName(layerName);
		        lyrDef.setTableName(tableName);
		        lyrDef.setWhereClause(whereClause);
		        lyrDef.setFieldGeometry(geomField);
		        lyrDef.setFieldNames(fields);
		        lyrDef.setFieldID(fidField);
		        lyrDef.setSRID_EPSG(strEPSG);
		        lyrDef.setConnection(conn);
		        if (driver instanceof ICanReproject)
		        {
		            ((ICanReproject)driver).setDestProjection(strEPSG);
		        }
		        ((DefaultJDBCDriver)driver).setData(conn, lyrDef);
		        IProjection proj = newProjection;//ahora, si no reproyecta la bbdd reproyecta el iterador
//		        if (driver instanceof ICanReproject)
//		        {
//		            proj = CRSFactory.getCRS("EPSG:" + ((ICanReproject)driver).getSourceProjection());
//		        }

		        return (FLyrVect) LayerFactory.createDBLayer(driver, layerName, proj);
			}catch(Exception e){
				e.printStackTrace();
			}
			return null;

	}


	//test para chequear la rapidez de los metodos de verificacion de candidatos en consultas
	//a partir de rectangulo
	public void test0(){
		String pol1 = "MULTIPOLYGON (((413434.195990259 4790622.699703139, 413441.7000601477 4790624.699360616, 413442.6978540559 4790620.700045662, 413435.2020303979 4790618.700388186, 413434.195990259 4790622.699703139, 413434.195990259 4790622.699703139)))";
		String pol2 = "MULTIPOLYGON (((411585.59723499016 4791781.20126231, 411575.39664767997 4791791.999412685, 411586.29816459515 4791802.297648691, 411596.4987519054 4791791.499498316, 411585.59723499016 4791781.20126231, 411585.59723499016 4791781.20126231)))";
		WKTReader reader = new WKTReader(new GeometryFactory());
		try {
			Geometry geo1 = reader.read(pol1);
			Geometry geo2 = reader.read(pol2);

			IGeometry igeo1 = FConverter.jts_to_igeometry(geo1);
			IGeometry igeo2 = FConverter.jts_to_igeometry(geo2);

			double xmin = 410000;
			double xmax = 415000;
			double ymin = 4790000;
			double ymax = 4793000;
			Rectangle2D rect = new Rectangle2D.Double(xmin, ymin, (xmax-xmin), (ymax-ymin));

			//PrecciseSpatialCheck
			 long t0 = System.currentTimeMillis();
			 int NUM_ITERATIONS = 50000;
			 for(int i = 0; i < NUM_ITERATIONS;i++){
				 igeo1.fastIntersects(rect.getMinX(),
						rect.getMinY(), rect.getWidth(), rect.getHeight());
				 igeo2.fastIntersects(rect.getMinX(),
							rect.getMinY(), rect.getWidth(), rect.getHeight());
			 }
			 long t1 = System.currentTimeMillis();
			 System.out.println((t1-t0)+" con precissespatialcheck");

			//FastSpatialCheck
			 Rectangle2D b1 = igeo1.getBounds2D();
			 Rectangle2D b2 = igeo2.getBounds2D();


			 long t2 = System.currentTimeMillis();
			 for(int i = 0; i < NUM_ITERATIONS;i++){
				 XRectangle2D.intersectInclusive(rect, b1);
				 XRectangle2D.intersectInclusive(rect, b2);
			 }
			 long t3 = System.currentTimeMillis();
			 System.out.println((t3-t2)+" con fastspatialcheck");

			 assertTrue((t3-t2) < (t1 - t0));
			//Spatial index check

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}




	//pruebas de iteracion para shp (vectorialfileadapter) y dxf (vectorialadapter)
	public void test1() {
		try {
			//pruebas de reproyeccion y seleccion de numero de campos
			FLyrVect lyr = (FLyrVect) newLayer("Cantabria.shp", SHP_DRIVER_NAME);
			lyr.setAvailable(true);

			//iteration selecting two fields
			IFeatureIterator iterator = lyr.getSource().getFeatureIterator(new String[]{"ID", "AREA"}, null);
			IFeature feature = iterator.next();
			assertTrue(feature != null);

			//iteration with reprojection
			iterator = lyr.getSource().getFeatureIterator((String[])null, newProjection);
			IFeature feature2 = iterator.next();
			//test of field restriction
			assertTrue(feature.getAttributes().length == 2);
			//si pasamos null, no devuelve ning?n campo
			assertTrue(feature2.getAttributes().length == 0);
			assertTrue(!feature.getGeometry().toJTSGeometry().equals(feature2.getGeometry().toJTSGeometry()));
			iterator.closeIterator();

			//pruebas de iteracion espacial
			FLyrVect lyr2 = (FLyrVect) newLayer("Edificaciones.shp", SHP_DRIVER_NAME);
			lyr2.setAvailable(true);
			double xmin = 410000;
			double xmax = 415000;
			double ymin = 4790000;
			double ymax = 4793000;
			Rectangle2D rect = new Rectangle2D.Double(xmin, ymin, (xmax-xmin), (ymax-ymin));

			//fast iteration
			long t0 = System.currentTimeMillis();
			iterator = lyr2.getSource().getFeatureIterator(rect, null, newProjection, true);
			int numFeatures = 0;
			while(iterator.hasNext()){
				feature = iterator.next();
				numFeatures++;
			}
			long t1 = System.currentTimeMillis();

			//vemos si hay diferencia entre el fast = true y el fast = false
			iterator = lyr2.getSource().getFeatureIterator(rect, null, newProjection, false);
			int numFeatures3 = 0;
			while(iterator.hasNext()){
				feature = iterator.next();
				numFeatures3++;
			}
			long t2 = System.currentTimeMillis();
			assertTrue(numFeatures3 <= numFeatures);
			iterator.closeIterator();
			System.out.println((t1-t0)+" en la iteracion rapida");
			System.out.println("Recuperados "+numFeatures);
			System.out.println((t2-t1)+" en la iteracion lenta");
			System.out.println("Recuperados "+numFeatures3);


			int numFeatures2 = 0;
			iterator = lyr2.getSource().getFeatureIterator();
			while(iterator.hasNext()){
				feature = iterator.next();
				numFeatures2++;
			}
			assertTrue(numFeatures2 > numFeatures);
			iterator.closeIterator();

			//pruebas de iteracion en base a criterios alfanumericos
			final String expr = "select * from " + lyr.getRecordset().getName() + " where area < 700;";
			iterator = lyr.getSource().getFeatureIterator(expr, null);
			numFeatures2 = 0;
			while(iterator.hasNext()){
				feature = iterator.next();
				numFeatures2++;
			}
			iterator.closeIterator();
			

			
			FLayer lyr53245 = LayerFactory.createLayer("/home/jaume/callestodo.shp",
					SHP_DRIVER_NAME,
					new File("/home/jaume/callestodo.shp"), PROJECTION_DEFAULT);
			
			iterator = ((FLyrVect) lyr53245).getSource().getFeatureIterator("select * from " + ((FLyrVect) lyr53245).getRecordset().getName() +" where (TIPCALLE = 'C' or  TIPCALLE='AV' or TIPCALLE='PL');", null);
			while(iterator.hasNext()){
				feature = iterator.next();
				numFeatures++;
			}
			iterator.closeIterator();
			
			iterator = lyr.getSource().getFeatureIterator();
			while(iterator.hasNext()){
				feature = iterator.next();
				numFeatures++;
			}
			assertTrue(numFeatures2 > 0);
			assertTrue(numFeatures > numFeatures2);
			iterator.closeIterator();

			//pruebas con el driver dxf en vez del shp
			FLyrVect lyr3 = (FLyrVect) newLayer("es1120003_2_02.dxf", DXF_DRIVER_NAME);
			lyr3.setAvailable(true);
			iterator = lyr3.getSource().getFeatureIterator("select * from "+lyr3.getRecordset().getName()+" where ID > 17;", null);
			numFeatures2 = 0;
			while(iterator.hasNext()){
				feature = iterator.next();
				numFeatures2++;
			}
			iterator.closeIterator();
			assertTrue(numFeatures2 > 0);

			//queda por probar los iteradores contra base de datos
			//en base a criterios alfanumericos y espaciales

		} catch (LoadLayerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ReadDriverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}



//	public void test3(){
//		try {
//			FLyrVect layer = (FLyrVect) newLayer("poly-valencia.shp", SHP_DRIVER_NAME);
//			IFeatureIterator iterator = layer.getSource().getFeatureIterator();
//			int numFeatures = 0;
//			while(iterator.hasNext()){
//				iterator.next();
//				numFeatures++;
//			}
//			assert(layer.getSource().getShapeCount() == numFeatures);
//
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

	//test of featureIterator with layer poly-valencia using a spatial filter.
	//jaume found a bug with an infinite loop iteration

	public void test4(){
		try {
			FLyrVect layer = (FLyrVect) newLayer("poly-valencia.shp", SHP_DRIVER_NAME);
			Rectangle2D extent = layer.getFullExtent();
			String[] fields = layer.getRecordset().getFieldNames();


			IFeatureIterator iterator = layer.
							getSource().
							getFeatureIterator(extent,fields, null, true);
			int numFeatures = 0;
			while(iterator.hasNext()){
				iterator.next();
				numFeatures++;
			}
			iterator.closeIterator();
			assert(layer.getSource().getShapeCount() == numFeatures);

		} catch (LoadLayerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ReadDriverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}





	public void test5(){
		try {
			int WIDTH = 300;
			int HEIGHT = 200;
			FLyrVect layer = (FLyrVect) newLayer("municipios.shp", SHP_DRIVER_NAME);
			ViewPort vp = new ViewPort(PROJECTION_DEFAULT);
			vp.setImageSize(new Dimension(WIDTH, HEIGHT));
			vp.setExtent(layer.getFullExtent());
			MapContext mapa = new MapContext(vp);
			mapa.getLayers().addLayer(layer);
			BufferedImage img = new BufferedImage(WIDTH, HEIGHT,
					BufferedImage.TYPE_INT_ARGB);

			long t0 = System.currentTimeMillis();
			mapa.draw(img, img.createGraphics(),mapa.getScaleView());
			long t1 = System.currentTimeMillis();
			System.out.println((t1-t0)+" en dibujar con MapContext.draw");


			long t2 = System.currentTimeMillis();
			layer.draw(img, img.createGraphics(),vp, new Cancellable(){
				public boolean isCanceled() {
					return false;
				}
				public void setCanceled(boolean canceled) {
				}}, getScaleView(vp));
			long t3 = System.currentTimeMillis();
			System.out.println((t3-t2)+" en dibujar con layer._draw");

		} catch (LoadLayerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ReadDriverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}





	//TODO Mover spatialindextest a esta clase
	public void test7(){

	}


	private long getScaleView(ViewPort viewPort) {
		double[] CHANGE = { 100000, 100, 1, 0.1, 160934.4,
				91.44, 30.48, 2.54, 1/8.983152841195214E-4 };
		Preferences prefsResolution = Preferences.userRoot().node( "gvsig.configuration.screen" );
		Toolkit kit = Toolkit.getDefaultToolkit();
		double dpi = prefsResolution.getInt("dpi",kit.getScreenResolution());
		IProjection proj = viewPort.getProjection();
		if (viewPort.getImageSize() == null)
			return -1;
		if (viewPort.getAdjustedExtent() == null) {
			return 0;
		}
		if (proj == null) {
			double w = ((viewPort.getImageSize().getWidth() / dpi) * 2.54);
			return (long) (viewPort.getAdjustedExtent().getWidth() / w * CHANGE[viewPort.getMapUnits()]);
		}

		return Math.round(proj.getScale(viewPort.getAdjustedExtent().getMinX(),
				viewPort.getAdjustedExtent().getMaxX(), viewPort.getImageSize()
						.getWidth(), dpi));
	}

}


