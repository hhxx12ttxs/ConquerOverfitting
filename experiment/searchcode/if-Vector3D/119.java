
package org.jlab.rec.ftof.geometry;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.jlab.geom.base.Detector;
import org.jlab.geom.component.ScintillatorPaddle;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.ftof.event.Panel;
import org.jlab.rec.ftof.utilities.Util;


/**
 * Stores the geometry constants of the detector.
 * <p>
 * Hierarchy is Detector->{@link PanelGeometry Panel}->{@link PaddleGeometry Paddle} geometries.
 * <p>
 * Queries for {@link PaddleGeometry PaddleGeometry} properties are directed at the relevant {@link PanelGeometry PanelGeometry} object
 * which holds all properties that are not unique to each and every paddle, and which passes
 * on the query to the relevant {@link PaddleGeometry PaddleGeometry} if needed. 
 * <p>
 * The raw data is read from the xml retrieved from the geometry service and stored in the structure given above.
 * Default values can be used instead for test purposes.
 * 
 * @author acolvill
 *
 */

public class FtofGeometry {

	
    private List<PanelGeometry> panelGeometryList;
	
   	
    /**
     * 
     * Creates an object to store geometry data retrieved from the
     * CLASFTOFDetector object
     * 
     * @param ftofDetector is CLASFTOFDetector object (in detector )
     */
   
    public FtofGeometry(Detector ftofDetector, String fName) {
        
    	panelGeometryList = new ArrayList<PanelGeometry>();
    	PanelGeometry panelGeom = null;
        
    	for (int sectorID = 1; sectorID <= 6; sectorID++) {	
            for (Panel.PanelType panelType : Panel.PanelType.values()) {
                int indSec=sectorID-1;
                int indSL = panelType.ID - 1;

                
                Vector3D normal = null;
                double  width=0, thickness=0;
                if(panelType.ID==1) {
                    normal = new Vector3D(0.422618, 0.0, 0.906308);
                    width=15.01;
                    thickness=5.08;
                }	
                if(panelType.ID==2) {
                    normal = new Vector3D(0.422618, 0.0, 0.906308);
                    width=6;
                    thickness=6;
                }
                if(panelType.ID==3) {
                    normal = new Vector3D(0.857167, 0.0, 0.515038);
                    width=22;
                    thickness=5.08;
                }
                Plane3D pl = ftofDetector.getSector(indSec).getSuperlayer(indSL).getLayer(0).getPlane();
                Vector3D normal2 = new Vector3D(pl.normal().x(), pl.normal().y(), pl.normal().z() );
                
                // test the geometry. Values from FTOFDetector
                // should reasonably coincide with default values 
                if(sectorID==1) {
                if(   Math.abs( (normal.x()-normal2.x()) ) > 0.10 
                   || Math.abs( (normal.y()-normal2.y()) ) > 0.10 
                   || Math.abs( (normal.z()-normal2.z()) ) > 0.10) {
                System.out.println(" !!! WRONG GEOMETRY. Bad Normals!!!");
                System.out.println(" SECTOR = " + sectorID + " PANEL = " + panelType.ID );
                System.out.println("  OLD geometry NORMAL = " 
                  + normal.x() +" " + normal.y() + " " + normal.z());
                System.out.println("  NEW geometry NORMAL = " 
                  + pl.normal().x() +" "+pl.normal().y()+" "+pl.normal().z());
                }}

                                
                double phi = (sectorID-1)*Math.PI/3.;
                double dirx = -Math.sin(phi);
                double diry =  Math.cos(phi);
                Vector3D direction = new Vector3D(dirx,diry,0);
                                        
                
                panelGeom = new PanelGeometry(panelType,sectorID,direction,
                                              normal,width,thickness,0,0);                    
                    
                for(int iPan = 1; iPan <= panelType.nPaddles; iPan++ ) {                            
                    int indPad = iPan-1;
    			
                    PaddleGeometry paddleGeom;
                        
                    double x = 0,y = 0,z = 0, length = 0;
                    if(panelType.ID==1) {
                        x = GeometryData.panel1aPaddleCentres[iPan-1][0];
                        y = GeometryData.panel1aPaddleCentres[iPan-1][1];
                        z = GeometryData.panel1aPaddleCentres[iPan-1][2];
                        length = GeometryData.panel1aHalfLengths[iPan-1]*2;
                    }
                    if(panelType.ID==2) {
                        x = GeometryData.panel1bPaddleCentres[iPan-1][0];
                        y = GeometryData.panel1bPaddleCentres[iPan-1][1];
                        z = GeometryData.panel1bPaddleCentres[iPan-1][2];
                        length = GeometryData.panel1bHalfLengths[iPan-1]*2;	
                    }	
                    if(panelType.ID==3) {
                        x = GeometryData.panel2PaddleCentres[iPan-1][0];
                        y = GeometryData.panel2PaddleCentres[iPan-1][1];
                        z = GeometryData.panel2PaddleCentres[iPan-1][2];
                        length = GeometryData.panel2HalfLengths[iPan-1]*2;	
                    }
                        
                    // Center point and length of a paddle
                    ScintillatorPaddle paddle = (ScintillatorPaddle) 
                      ftofDetector.getSector(indSec).getSuperlayer(indSL).getLayer(0).getComponent(indPad);
                    Point3D p = 
                      ftofDetector.getSector(indSec).getSuperlayer(indSL).getLayer(0).getComponent(indPad).getMidpoint();
                    Vector3D dir = paddle.getDirection();
                    double len = 
                      ftofDetector.getSector(indSec).getSuperlayer(indSL).getLayer(0).getComponent(indPad).getLength();
                    
                      //ftofDetector.getSector(indSec).getSuperLayer(indSL).getLayer(0).getSensor(i)
                      
                    
                                
                    // test the geometry. Values from FTOFDetector
                    // should reasonably coincide with default values 
                    double del = 0.30;
                    if(indSL==1 && indPad==1) del=0.8;
                    if(indSL==1 && indPad==3) del=0.8;
                    if(sectorID==1) {
                    if(   Math.abs( (p.x()-x)/x ) > 0.20 
                       || Math.abs( (p.y()-y)/y ) > 0.20 
                       || Math.abs( (p.z()-z)/z ) > 0.20 
                       || Math.abs( (len-length)/length ) > del ) {
                        System.out.println(" !!! WRONG GEOMETRY. Bad paddle center and/or width !!!");
                        System.out.println(" SECTOR = " + sectorID + " PANEL = " 
                          + panelType.ID + "  PADDLE = " + iPan);
                        System.out.println("  OLD geometry COORD = " 
                          + x +" " + y + " " + z + " " + length);
                        System.out.println("  NEW geometry COORD = " 
                          + p.x() +" " + p.y() + " " + p.z() + " " + len);
                    }}
                        
                        
                    paddleGeom = new PaddleGeometry(new Vector3D(p.x(),p.y(),p.z()), len);
                    panelGeom.addPaddleGeometry(paddleGeom);
    				        
                    }
    			
                panelGeometryList.add(panelGeom);
    			
    		}
            
    	}
    	
        Util.prn(10,"  Geometry object created using " + fName);
        Util.prn(10,"  !ATTENTION! widths and thiknesses are hardcoded");

    }

    
    /**
     * 
     * @return a list of all PanelGeometry objects
     */
    
    public List<PanelGeometry> getPanelGeometry() { return panelGeometryList; }
  
    
    /**
     *Returns the panelType geometry of sector with given sector ID and panelType name  
     * 
     * @param sector , the sector ID (1-6)
     * @param panel , the panelType name
     * @return the PanelGeometry object 
     */
           
    public PanelGeometry getPanelGeometry(int sector, Panel.PanelType panel)
    {	
    	// sector and panelType are assumed to exist, due to previous exception causing checks	
    	for(PanelGeometry panelGeom: panelGeometryList) {
            if((panelGeom.getSector()==sector)&&(panelGeom.getPanelName().equals(panel))){
                return(panelGeom);
            }
       	}
    	return(null);
    }
    
    
    ///**
    // * Converts a CLAS coordinate to the equivalent sector coordinate
    // * 
    // * @param CLAScoord , the CLAS coordinate to be converted
    // * @param sector , the sector id 
    // * @return the sector coordinate
    // */
    
    //public static Vector3D CLAS2sector(Vector3D CLAScoord, int sector) {	
    //   	double rotationAngle = ( 60.0 * (sector-1) / 180.0 ) * Math.PI;
    //	double x = CLAScoord.x() * Math.cos(rotationAngle) + CLAScoord.y() * Math.sin(rotationAngle);
    //	double y = - CLAScoord.x() * Math.sin(rotationAngle) + CLAScoord.y() * Math.cos(rotationAngle);
    //	return ( new Vector3D( x,y,CLAScoord.z() ) );
    //}
    
    
    ///**
    // * Converts a sector coordinate to the equivalent CLAS coordinate
    // * 
    // * @param CLAScoord , the CLAS coordinate to be converted
    // * @param sector , the sector id 
    // * @return the sector coordinate
    // */
    
    //public static Vector3D sector2CLAS(Vector3D CLAScoord, int sector) {		
    //   	double rotationAngle = - ( 60.0 * (sector-1) / 180.0 ) * Math.PI;
    //	double x = CLAScoord.x() * Math.cos(rotationAngle) + CLAScoord.y() * Math.sin(rotationAngle);
    //	double y = - CLAScoord.x() * Math.sin(rotationAngle) + CLAScoord.y() * Math.cos(rotationAngle);
    //	return ( new Vector3D( x,y,CLAScoord.z() ) );
    //}
    
    
    @Override
    public String toString()
    {
    	String returnString="";
   	for (PanelGeometry panelGeom : getPanelGeometry()) {                
            returnString = returnString.concat(panelGeom.toString());
	}       
      	return returnString;
    }
   	
}





/**
 * Temporary class to hold geometry data from mathematica.  If using the geometry serivce xml, these values are not used.
 * Everything in cm.
 * 
 * @author acolvill
 *
 */

class GeometryData {
	
	public static final double[]  panel1aHalfLengths  = {   
            16.1417, 24.0696, 31.9975, 39.9255, 47.8534,
            53.2413, 61.1692, 69.0971, 77.0251, 84.953,
            92.8809, 100.809, 108.737, 116.665, 124.593,
            132.52, 140.448, 148.376, 156.304, 164.232,
            172.16,  180.088,  188.016 
        };
	
	
	public static final double[]  panel1bHalfLengths =  {   
            7.6073, 7.6073, 13.9192, 13.9192, 20.4597,
            20.4597, 26.9875, 26.9875, 33.528, 33.528,
            40.0558, 40.0558, 46.5836, 46.5836, 53.1241,
            53.1241, 59.6519, 59.6519, 66.1797, 66.1797,
            72.7202, 72.7202, 79.248, 79.248, 85.7758,
            85.7758, 92.3163, 92.3163, 98.8441, 98.8441,
            105.372, 105.372, 111.912, 111.912, 118.44,
            118.44, 124.968, 124.968, 131.509, 131.509,
            138.036, 138.036, 144.564, 144.564, 151.105,
            151.105, 157.632, 157.632, 164.173, 164.173,
            170.701, 170.701, 177.229, 177.229, 183.769,
            183.769, 190.297, 190.297, 190.297, 190.297,
            190.297, 190.297
        };
	
	public static final double[]  panel2HalfLengths	=   { 
            185.649,192.507,199.365,206.235,213.093 
        };	
	
	public static final double[][] panel1bPaddleCentres = { 
            {61.6712, 0.0, 689.61},
            {67.1576, 0.0, 687.07},
            {72.6694, 0.0, 684.505},
            {78.3082, 0.0, 681.888},
            {83.9724, 0.0, 679.247},
            {89.6112, 0.0, 676.605},
            {95.3008, 0.0, 673.964},
            {100.94, 0.0, 671.347},
            {106.604, 0.0, 668.68},
            {112.243, 0.0, 666.064},
            {117.932, 0.0, 663.423},
            {123.546, 0.0, 660.781},
            {129.235, 0.0, 658.139},
            {134.874, 0.0, 655.498},
            {140.538, 0.0, 652.856},
            {146.177, 0.0, 650.24},
            {151.867, 0.0, 647.573},
            {157.505, 0.0, 644.957},
            {163.17, 0.0, 642.315},
            {168.808, 0.0, 639.674},
            {174.498, 0.0, 637.032},
            {180.137, 0.0, 634.416},
            {185.801, 0.0, 631.749},
            {191.44, 0.0, 629.133},
            {197.129, 0.0, 626.491},
            {202.743, 0.0, 623.849},
            {208.432, 0.0, 621.208},
            {214.071, 0.0, 618.592},
            {219.761, 0.0, 615.925},
            {225.374, 0.0, 613.308},
            {231.064, 0.0, 610.641},
            {236.703, 0.0, 608.025},
            {242.367, 0.0, 605.384},
            {248.006, 0.0, 602.742},
            {253.695, 0.0, 600.1},
            {259.334, 0.0, 597.484},
            {264.998, 0.0, 594.817},
            {270.637, 0.0, 592.201},
            {276.327, 0.0, 589.559},
            {281.965, 0.0, 586.918},
            {287.63, 0.0, 584.276},
            {293.268, 0.0, 581.66},
            {298.958, 0.0, 578.993},
            {304.571, 0.0, 576.377},
            {310.261, 0.0, 573.71},
            {315.9, 0.0, 571.094},
            {321.589, 0.0, 568.452},
            {327.203, 0.0, 565.836},
            {332.892, 0.0, 563.169},
            {338.531, 0.0, 560.553},
            {344.195, 0.0, 557.886},
            {349.834, 0.0, 555.269},
            {355.524, 0.0, 552.628},
            {361.163, 0.0, 549.986},
            {366.827, 0.0, 547.345},
            {372.466, 0.0, 544.728},
            {378.155, 0.0, 542.061},
            {383.794, 0.0, 539.445},
            {390.794, 0.0, 537.445},
            {395.794, 0.0, 535.445},
            {400.794, 0.0, 534.445},
            {405.794, 0.0, 532.445}
        };

	
	public static final double[][] panel1aPaddleCentres = { 
            {76.1111, 0.0, 696.554},
            {89.8427, 0.0, 690.151},
            {103.574, 0.0, 683.748},
            {117.306, 0.0, 677.345},
            {131.037, 0.0, 670.942},
            {144.769, 0.0, 664.539},
            {158.5, 0.0, 658.136},
            {172.232, 0.0, 651.732},
            {185.964, 0.0, 645.329},
            {199.695, 0.0, 638.926},
            {213.427, 0.0, 632.523},
            {227.158, 0.0, 626.12},
            {240.89, 0.0, 619.717},
            {254.621, 0.0, 613.314},
            {268.353, 0.0, 606.911},
            {282.084, 0.0, 600.507},
            {295.816, 0.0, 594.104},
            {309.548, 0.0, 587.701},
            {323.279, 0.0, 581.298},
            {337.011, 0.0, 574.895},
            {350.742, 0.0, 568.492},
            {364.474, 0.0, 562.089},
            {378.205, 0.0, 555.685}
        };
	
	public static final double[][] panel2PaddleCentres = 	{	
            {381.367, 0.0, 523.647},
            {392.91, 0.0, 504.568},
            {404.595, 0.0, 485.576},
            {415.996, 0.0, 466.412},
            {427.542, 0.0, 447.33}
        };

}


