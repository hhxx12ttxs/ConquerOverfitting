package org.jlab.rec.ftof.geometry;

import java.util.ArrayList;
import java.util.List;

import org.jlab.geom.prim.Vector3D;


import org.jlab.rec.ftof.event.Panel;

/**
 *  Stores the geometry data for a single panel,
 *  which consists of the following:
 *  
 *  <ul>
 *  <li> sector ID
 *  <li>{@link Panel Panel} name
 *  <li> panel direction vector
 *  <li> panel normal vector
 *  <li> width of paddles on panel
 *  <li> thickness of paddles on panel
 *  <li> extent in x direction of paddles on panel
 *  <li> extent in z direction of paddles on panel
 *  <li>a list of {@link PaddleGeometry PaddleGeometry} objects in this sector and panel
 *  </ul>
 *  <p>
 *  All vectors in sector coordinates.  All lengths in cm.  To get geometry properties for a particular paddle, you query the PanelGeometry object and it 
 *  returns the information directly, or it will query the {@link PaddleGeometry PaddleGeometry} object for
 *  properties that are unique to each paddle (paddle centre vector, paddle length).
 * 
 * @author acolvill
 *
 */

public class PanelGeometry {
	
	
	private int sector;
	private Panel.PanelType panel;

	private List<PaddleGeometry> paddleGeometryList;
	
	private Vector3D normalVector;
	private Vector3D directionVector;
        
        
	
	private double width;
	private double thickness;
	
	private double extentX;
	private double extentZ;
	
	
	/**
	 * Creates object to store geometry data for a single panel
	 * 
	 * @param panel
	 * @param sector
	 * @param directionVector
	 * @param normalVector
	 * @param width
	 * @param thickness
	 */
	
	public PanelGeometry(Panel.PanelType panel, int sector, 
                             Vector3D directionVector, Vector3D normalVector, 
                             double width, double thickness, double extentX, 
                             double extentZ) {
		this.panel = panel;
		this.sector = sector;
		this.directionVector = directionVector;
		this.normalVector = normalVector;
		this.width = width;
		this.thickness = thickness;
		this.extentX = extentX;
		this.extentZ = extentZ;
		paddleGeometryList = new ArrayList<PaddleGeometry>();
	}
	
	/**
	 * 
	 * @return  the Panel object
	 */

	
	public Panel.PanelType getPanelName() {
	
		return panel;
		
	}
	
	
	/**
	 * Adds a paddle geometry object to the paddleGeometryList
	 *  
	 * 
	 *@param paddleGeom
	 */
	
	public void addPaddleGeometry(PaddleGeometry paddleGeom) {
		
		paddleGeometryList.add(paddleGeom);
		
	}
	
	
	/**
	 * Gets the length of the paddle with the given ID.
	 * 
	 * This information is contained in the corresponding PaddleGeometry object.
	 * 
	 * @param id , the paddle ID
	 * @return the length
	 */
	
	public double getLength(int id) {
		
		if (id <= 0 || id > paddleGeometryList.size())
            throw new ArrayIndexOutOfBoundsException("Wrong paddle ID: " + id);
		
		return ( paddleGeometryList.get(id-1).getLength() );
		
	}
	
	
	/**
	 * Gets a vector pointing to the center of the paddle with the given paddle ID.  Sector coordinates.
	 * 
	 * This information is contained in the corresponding PaddleGeometry object.
	 * 
	 * @param id , the paddle ID
	 * @return the center vector
	 */
		
	public Vector3D getCentreVector(int id) {
		
		if (id <= 0 || id > paddleGeometryList.size())
            throw new ArrayIndexOutOfBoundsException("Wrong paddle ID: " + id);
		
		return ( paddleGeometryList.get(id-1).getCentre() );
		
	}
	
	
	/**
	 * Gets the width of any paddle on this paddle.
	 * Assumed to be the same for all paddles on the panel.
	 * 
	 * @return the paddle width
	 */
		
	public double getWidth() {
		
		return width;
		
	}
	
	
	/**
	 * Gets the thickness of any paddle on this panel.
	 * Assumed to be the same for all paddles on the panel.
	 * 
	 * @return the paddle thickness
	 */
	
	
	public double getThickness() {
		
		return thickness;
		
	}
	
	
	/**
	 * Gets a vector pointing normal to the plane of the panel, away from the target.  Sector coordinates.
	 * This is assumed the same for all paddles on the panel.
	 * 
	 * @return the panel normal vector
	 */
	
	
	public Vector3D getNormalVector() {
		
		return normalVector;
		
	}

	
	/**
	 * Gets a vector pointing clockwise, along the length of a paddle on this panel.  Sector coordinates.
	 * Assumed to be same for all paddles on the panel.
	 * 
	 * @return the panel direction vector
	 */
	
	public Vector3D getDirectionVector() {
		
		return directionVector;
		
	}
	
	
	/**
	 * 
	 * 
	 * @return the sector of this PanelGeometry object
	 */
	
	
	public int getSector() {
		
		return sector;
		
	}
	
	
	/**
	 * 
	 * @return the extent of any paddle on this panel along the x axis (sector coordinates)
	 */
	
	public double getExtentX() {
		return extentX;
	}

	
	/**
	 * 
	 * @return the extent of any paddle on this panel along the z axis (sector coordinates)
	 */

	public double getExtentZ() {
		return extentZ;
	}

	
	/**
	 * Returns a string describing the whole panel's geometry
	 * 
	 */
	
	@Override
    public String toString()
    {
		
		String returnString="";
		
		for (int i = 1; i <= paddleGeometryList.size(); i++ ) {
		
			returnString = returnString.concat(String.format(
				 "sector          : %1.0f" + "%n" +
			     "panel           : " + panel + "%n" +
       			 "paddle id       : %2.0f" + "%n" +
       			 "length          : %11.6f" + "%n" +
       			 "width           : %11.6f" + "%n" +
       			 "thickness       : %11.6f" + "%n" +
       			 "centre x,y,z    : %11.6f %11.6f %11.6f" + "%n" +
       			 "normal x,y,z    : %11.6f %11.6f %11.6f" + "%n" +
       			 "direction x,y,z : %11.6f %11.6f %11.6f" + "%n" +
       			 "extent_x        : %11.6f" + "%n" +
       			 "extent_z        : %11.6f",
       			 1.0*getSector(),
       			 i*1.0,
       			 getLength(i),
       			 getWidth(),
       			 getThickness(),
       			 getCentreVector(i).x(),
       			 getCentreVector(i).y(),
       			 getCentreVector(i).z(),
       			 getNormalVector().x(),
       			 getNormalVector().y(),
       			 getNormalVector().z(),
       			 getDirectionVector().x(),
       			 getDirectionVector().y(),
       			 getDirectionVector().z(),
       			 getExtentX(),
       			 getExtentZ()  ));
			
		}
			
		return returnString;
    }
	
	
	/**
	 * Returns a string describing a single paddle's geometry
	 * 
	 */
	
	public String toString(int i)
    {
				
		return (String.format(
				 "ID              : %3.0f" + "%n" +
				 "length          : %11.6f" + "%n" +
      			 "width           : %11.6f" + "%n" +
      			 "thickness       : %11.6f" + "%n" +
      			 "centre x,y,z    : %11.6f %11.6f %11.6f" + "%n" +
      			 "normal x,y,z    : %11.6f %11.6f %11.6f" + "%n" +
      			 "direction x,y,z : %11.6f %11.6f %11.6f"+ "%n" +
      			 "extent_x        : %11.6f" + "%n" +
       			 "extent_z        : %11.6f",
      			 i*1.0,
      			 getLength(i),
      			 getWidth(),
      			 getThickness(),
      			 getCentreVector(i).x(),
      			 getCentreVector(i).y(),
      			 getCentreVector(i).z(),
      			 getNormalVector().x(),
      			 getNormalVector().y(),
      			 getNormalVector().z(),
      			 getDirectionVector().x(),
      			 getDirectionVector().y(),
      			 getDirectionVector().z(),
      			 getExtentX(),
       			 getExtentZ()  ));

     }


	public int getNPaddles() {
		
		return paddleGeometryList.size();
	}
	
}
	

	



