<<<<<<< HEAD
/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2008, Refractions Research Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package net.refractions.udig.catalog.wmsc.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.refractions.udig.catalog.internal.wms.WmsPlugin;

import org.geotools.data.ows.CRSEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.util.ObjectCache;
import org.geotools.util.ObjectCaches;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Class represents a WMSC tile set.  See: 
 * <p>
 * http://wiki.osgeo.org/wiki/WMS_Tiling_Client_Recommendation#GetCapabilities_Responses
 *  </p>
 *  
 * @author Emily Gouge, Graham Davis (Refractions Research, Inc.)
 * @since 1.2.0
 */
public class WMSTileSet implements TileSet {

    /** Not fear factor! The factor applied to determine what scale zoom levels should switch at */
    private static final double SCALE_FACTOR = 0.2;

    /** a unique identifies */
    private int id;
    
    /** the TiledWebMapServer **/
    private TiledWebMapServer server;

	/** Coordinate Reference System of the Tiles */
    private CoordinateReferenceSystem crs;
    /**
     * SRSName (usually of the format "EPSG:4326")
     */
    private String epsgCode;

    /** Data bounding box **/
    private ReferencedEnvelope bboxSrs;

    /** size of tiles - in pixels (often 512) */
    private int width;

    /** size of tiles - in pixels (often 512) */
    private int height;

    /** image format - MIME type? */
    private String format;

    /** List of layers (separated by comma?) */
    private String layers;

    /** Comma seperated list of resolutions in units per pixel */
    private String resolutions;

    /** Parsed out resolutions - the strict values from resolutions */
    private double[] dresolutions;

    /**
     * Parsed out resolutions - the relaxed values so we do not request more data that can be drawn
     * per pixel. (Often this amounts 1.2 real pixels per on screen pixel - see SCALE_FACTOR
     */
    private double[] mresolutions; // the scale at which we will switch zoom levels

    /** styles */
    private String styles;

    /** map of tiles 
     * NOTE:  This is a WEAKHashMap because we don't want to run out of
     * memory storing all the tiles.  The garbage collector should clean
     * up less-used keys and their objects as necessary. 
     **/
    ObjectCache tiles = ObjectCaches.create("soft", 50); //Tiles that are on the screen //$NON-NLS-1$
    

    public WMSTileSet() {
        updateID();
    }

    /* (non-Javadoc)
	 * @see net.refractions.udig.catalog.wmsc.server.TileSet#setCoorindateReferenceSystem(java.lang.String)
	 */
    public void setCoorindateReferenceSystem( String epsg ) {
        this.epsgCode = epsg;
        try {
            this.crs = CRS.decode(epsg);
        } catch (Exception ex) {
            // WmsPlugin.trace("Cannot decode tile epsg code: " + epsg, ex); //$NON-NLS-1$
        }
        updateID();
    }

    /* (non-Javadoc)
	 * @see net.refractions.udig.catalog.wmsc.server.TileSet#getCoordinateReferenceSystem()
	 */
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return this.crs;
    }

    /* (non-Javadoc)
	 * @see net.refractions.udig.catalog.wmsc.server.TileSet#setBoundingBox(org.geotools.data.ows.CRSEnvelope)
	 */
    public void setBoundingBox( CRSEnvelope bbox ) {
        CoordinateReferenceSystem crs = null;
        try {
            crs = CRS.decode(bbox.getEPSGCode());
        } catch (Exception ex) {
            System.out.println("Cannot decode tile epsg code: " + bbox.getEPSGCode()); //$NON-NLS-1$
        }
        bboxSrs = new ReferencedEnvelope(bbox.getMinX(), bbox.getMaxX(), bbox.getMinY(), bbox
                .getMaxY(), crs);
        updateID();
    }
    /* (non-Javadoc)
	 * @see net.refractions.udig.catalog.wmsc.server.TileSet#setWidth(int)
	 */
    public void setWidth( int width ) {
        this.width = width;
        updateID();
    }

    /* (non-Javadoc)
	 * @see net.refractions.udig.catalog.wmsc.server.TileSet#setStyles(java.lang.String)
	 */
    public void setStyles( String styles ) {
        this.styles = styles;
        updateID();
    }
    /* (non-Javadoc)
	 * @see net.refractions.udig.catalog.wmsc.server.TileSet#setHeight(int)
	 */
    public void setHeight( int height ) {
        this.height = height;
        updateID();
    }

    /* (non-Javadoc)
	 * @see net.refractions.udig.catalog.wmsc.server.TileSet#setFormat(java.lang.String)
	 */
    public void setFormat( String format ) {
        this.format = format;
        updateID();
    }
    /* (non-Javadoc)
	 * @see net.refractions.udig.catalog.wmsc.server.TileSet#setLayers(java.lang.String)
	 */
    public void setLayers( String layers ) {
        this.layers = layers;
        updateID();
    }

    /* (non-Javadoc)
	 * @see net.refractions.udig.catalog.wmsc.server.TileSet#setResolutions(java.lang.String)
	 */
    public void setResolutions( String res ) {
        this.resolutions = res;
        String[] sres = resolutions.split(" "); //$NON-NLS-1$

        double[] dres = new double[sres.length];
        for( int i = 0; i < sres.length; i++ ) {
            dres[i] = Double.parseDouble(sres[i]);
        }
        this.dresolutions = dres;

        // compute resolutions where the zoom will switch
        mresolutions = new double[dresolutions.length - 1];
        for( int i = 0; i < dresolutions.length - 1; i++ ) {
            mresolutions[i] = ((dresolutions[i] - dresolutions[i + 1]) * SCALE_FACTOR)
                    + dresolutions[i + 1];
        }
        updateID();
    }

    /* (non-Javadoc)
	 * @see net.refractions.udig.catalog.wmsc.server.TileSet#getNumLevels()
	 */
    public int getNumLevels() {
        return this.dresolutions.length;
    }

    /**
     * Given a scale factor for the resulting image it finds the zoom level that matches the scale
     * factor best.
     * 
     * @param scale
     * @return resolution of the zoom level that best matches the scale factor
     */
    private double findAppropriateZoomLevel( double scale ) {

        if (scale > mresolutions[0]) {
            return dresolutions[0];
        }
        for( int i = 1; i < mresolutions.length; i++ ) {
            if (mresolutions[i - 1] >= scale && mresolutions[i] < scale) {
                return dresolutions[i];
            }
        }
        // maximum zoom
        return this.dresolutions[this.dresolutions.length - 1];
    }

    /**
     * Creates a wmsc query string from the given bounds.
     * <p>
     * This string is *very* carefully constructed with the assumption that all the getFormat(),
     * getEPSG(), getLayers() methods return Strings that are valid , consistent and ready to go.
     * 
     * @param tile
     * @return
     */
    @SuppressWarnings("nls")
    public String createQueryString( Envelope tile ) {
        String query = "service=WMS&request=getMap&tiled=true&width="+width+"&height="+height+"&format=" + getFormat() + "&srs=" + getEPSGCode()
                + "&layers=" + getLayers() + "&bbox=" + tile.getMinX() + "," + tile.getMinY() + ","
                + tile.getMaxX() + "," + tile.getMaxY() + "&styles=" + getStyles();
        return query;
    }

    /* (non-Javadoc)
	 * @see net.refractions.udig.catalog.wmsc.server.TileSet#getTilesFromViewportScale(com.vividsolutions.jts.geom.Envelope, double)
	 */
    public Map<String, Tile> getTilesFromViewportScale( Envelope bounds, double viewportScale ) {
        double scale = findAppropriateZoomLevel(viewportScale);
        return getTilesFromZoom(bounds, scale);
    }
    
    /* (non-Javadoc)
	 * @see net.refractions.udig.catalog.wmsc.server.TileSet#getTilesFromZoom(com.vividsolutions.jts.geom.Envelope, double)
	 */
    public Map<String, Tile> getTilesFromZoom( Envelope bounds, double zoom ) {

        double xscale = width * zoom;
        double value = bounds.getMinX() - bboxSrs.getMinX();
        
        double minx = Math.floor(value / xscale) * xscale + bboxSrs.getMinX();
        value = bounds.getMaxX() - bboxSrs.getMinX();
        double maxx = Math.ceil(value / xscale) * xscale + bboxSrs.getMinX();

        double yscale = height * zoom;
        value = bounds.getMinY() - bboxSrs.getMinY();
        double miny = Math.floor(value / yscale) * yscale + bboxSrs.getMinY();
        value = bounds.getMaxY() - bboxSrs.getMinY();
        double maxy = Math.ceil(value / yscale) * yscale + bboxSrs.getMinY();
        Map<String, Tile> viewportTiles = new HashMap<String, Tile>();
      
        int xNum = (int)Math.round((maxx- minx) / xscale);
        int yNum = (int)Math.round((maxy - miny) / yscale);
        for (int x = 0; x < xNum; x++){
            double xmin = roundDouble(x * xscale + minx);
            double xmax = roundDouble((x+1) * xscale + minx);
            for (int y = 0; y < yNum; y ++){
                double ymin = roundDouble(y * yscale + miny);
                double ymax = roundDouble((y+1) * yscale + miny);
                //Envelope e = new Envelope(x*xscale+minx, (x+1) * xscale+minx, y * yscale+miny, (y +1)* yscale + miny);
                Envelope e = new Envelope(xmin, xmax, ymin, ymax);
                if (e.getMaxX() <= bboxSrs.getMinX() || e.getMinX() >= bboxSrs.getMaxX()
                        || e.getMaxY() <= bboxSrs.getMinY() || e.getMinY() >= bboxSrs.getMaxY()) {
                    // outside of bounds ignore
                } else {
                    // tile is within the bounds, create it if necessary and
                    // add it to the map
                    String tileid = WMSTile.buildId(e, zoom);
                    Tile tile;
                    if (tiles.peek(tileid) == null || tiles.get(tileid) == null) {
                        tile = new WMSTile(server, this, e, zoom);
                        tiles.put(tileid, tile);
                        // create the tile position within the tilerange grid for this scale
                        double topleft_x = bboxSrs.getMinX();
                        double topleft_y = bboxSrs.getMaxY();
                        double tileleft_x = e.getMinX();
                        double tileleft_y = e.getMaxY();

                        double spacex = tileleft_x - topleft_x; // x is left to right
                        double spacey = topleft_y - tileleft_y; // y is top to bottom

                        int posx = (int) Math.round(spacex / xscale);
                        int posy = (int) Math.round(spacey / yscale);

                        String position = posx + "_" + posy; //$NON-NLS-1$
                        tile.setPosition(position);
                    } else {
                        tile = (Tile) tiles.get(tileid);
                    }
                    viewportTiles.put(tileid, tile);
                }
            }
        }
        return viewportTiles;
    }    
    
    
    /**
     * This function takes the last two digits (8 bits) of a double and 0's them. 
     * 
     *
     * @param number
     * @return
     */
    private static double roundDouble(double number){
        Long xBits = Double.doubleToLongBits(number);
        //zeroLowerBits
        int nBits = 8;
        long invMask = (1L << nBits) - 1L;
        long mask =~ invMask;
        xBits &= mask;   
        return Double.longBitsToDouble(xBits);
    }
    
    /**
     *  Break up the bounds for this zoom level into a list of bounds so that no single
     *  bounds has more than 1024 tiles in it.
     *  
	 * @see net.refractions.udig.catalog.wmsc.server.TileSet#getBoundsListForZoom(com.vividsolutions.jts.geom.Envelope, double)
	 */
    public List<Envelope> getBoundsListForZoom( Envelope bounds, double zoom ) {

    	int maxTilesPerBound = 1024;
    	List<Envelope> boundsList = new ArrayList<Envelope>();

        double xscale = width * zoom;
        double value = bounds.getMinX() - bboxSrs.getMinX();
        
        double minx = Math.floor(value / xscale) * xscale + bboxSrs.getMinX();
        value = bounds.getMaxX() - bboxSrs.getMinX();
        double maxx = Math.ceil(value / xscale) * xscale + bboxSrs.getMinX();

        double yscale = height * zoom;
        value = bounds.getMinY() - bboxSrs.getMinY();
        double miny = Math.floor(value / yscale) * yscale + bboxSrs.getMinY();
        value = bounds.getMaxY() - bboxSrs.getMinY();
        double maxy = Math.ceil(value / yscale) * yscale + bboxSrs.getMinY();
        long tilesPerRow = Math.round((maxx-minx) / xscale);
        long tilesPerCol = Math.round((maxy-miny) / yscale);
        long totalTiles = tilesPerCol * tilesPerRow;
    	
         // if there are not enough tiles to make 1024 for this zoom and bounds, then
        // return the single bounds
    	if ( totalTiles <=  maxTilesPerBound ) {
    		boundsList.add(bounds);
    		return boundsList;
    	}
    	
    	// create the size of each bounds
    	double scaleDownFactor = Math.ceil(totalTiles / maxTilesPerBound);
    	double boundsWidth = Math.ceil(tilesPerRow / scaleDownFactor) * xscale;
    	double boundsHeight = Math.ceil(tilesPerCol / scaleDownFactor) * yscale;
    	
    	// create each bounds
    	double x = minx;
    	while (x <= maxx) {
    		double y = miny;
    		while (y <= maxy) {
    			double x2 = x + boundsWidth;
    			if (x2 > maxx) x2 = maxx;
    			double y2 = y + boundsHeight;
    			if (y2 > maxy) y2 = maxy;
    			boundsList.add(new Envelope(x, x2, y, y2));
    			y += boundsHeight;
    		}
    		x += boundsWidth;
    	}
        
        return boundsList;
    }
    
    /* (non-Javadoc)
	 * @see net.refractions.udig.catalog.wmsc.server.TileSet#getTileCount((com.vividsolutions.jts.geom.Envelope, double))
	 */
    public long getTileCount( Envelope bounds, double zoom ) {
        double xscale = width * zoom;
        double value = bounds.getMinX() - bboxSrs.getMinX();
        
        double minx = Math.floor(value / xscale) * xscale + bboxSrs.getMinX();
        value = bounds.getMaxX() - bboxSrs.getMinX();
        double maxx = Math.ceil(value / xscale) * xscale + bboxSrs.getMinX();

        double yscale = height * zoom;
        value = bounds.getMinY() - bboxSrs.getMinY();
        double miny = Math.floor(value / yscale) * yscale + bboxSrs.getMinY();
        value = bounds.getMaxY() - bboxSrs.getMinY();
        double maxy = Math.ceil(value / yscale) * yscale + bboxSrs.getMinY();
        long tilesPerRow = Math.round((maxx-minx) / xscale);
        long tilesPerCol = Math.round((maxy-miny) / yscale);
        return tilesPerCol * tilesPerRow;
    }

    /* (non-Javadoc)
	 * @see net.refractions.udig.catalog.wmsc.server.TileSet#getLayers()
	 */
    public String getLayers() {
        return this.layers;
    }

    /* (non-Javadoc)
	 * @see net.refractions.udig.catalog.wmsc.server.TileSet#getStyles()
	 */
    public String getStyles() {
        return this.styles;
    }

    /* (non-Javadoc)
	 * @see net.refractions.udig.catalog.wmsc.server.TileSet#getFormat()
	 */
    public String getFormat() {
        return this.format;
    }
    /* (non-Javadoc)
	 * @see net.refractions.udig.catalog.wmsc.server.TileSet#getEPSGCode()
	 */
    public String getEPSGCode() {
        return this.epsgCode;
    }
    /* (non-Javadoc)
	 * @see net.refractions.udig.catalog.wmsc.server.TileSet#getWidth()
	 */
    public int getWidth() {
        return this.width;
    }
    /* (non-Javadoc)
	 * @see net.refractions.udig.catalog.wmsc.server.TileSet#getHeight()
	 */
    public int getHeight() {
        return this.height;
    }
    /* (non-Javadoc)
	 * @see net.refractions.udig.catalog.wmsc.server.TileSet#getBounds()
	 */
    public ReferencedEnvelope getBounds() {
        return this.bboxSrs;
    }

    /* (non-Javadoc)
	 * @see net.refractions.udig.catalog.wmsc.server.TileSet#getId()
	 */
    public int getId() {
        return this.id;
    }
    
    /* (non-Javadoc)
	 * @see net.refractions.udig.catalog.wmsc.server.TileSet#getResolutions()
	 */
    /**
     * @returns a copy of the resolutions array
     */
    public double[] getResolutions(){
        double[] d = new double[dresolutions.length];
        System.arraycopy(dresolutions, 0, d, 0, d.length);
        return d;
//        return Arrays.copyOf(this.dresolutions, this.dresolutions.length);
    }
    
    /**
     * Create a unique identifier for the tileset from the various strings that define the tile set.
     */
    private void updateID(){
        //compute a hashset from the attributes;
        StringBuffer sb = new StringBuffer();
        
        if (this.epsgCode != null)
            sb.append(epsgCode);
        
        if (this.format != null)
            sb.append(this.format);
        
        if (this.layers != null)
            sb.append(layers);
        
        if (this.resolutions != null)
            sb.append(this.resolutions);
        if (this.styles != null)
            sb.append(this.styles);
        if (this.bboxSrs != null){
            sb.append(this.bboxSrs.getMinX());
            sb.append(this.bboxSrs.getMaxX());
            sb.append(this.bboxSrs.getMinY());
            sb.append(this.bboxSrs.getMaxY());
            sb.append(this.bboxSrs.getMinX());
        }
        if (this.bboxSrs != null && this.bboxSrs.getCoordinateReferenceSystem() != null) {
            sb.append(this.bboxSrs.getCoordinateReferenceSystem().toString().hashCode());
        }
        
        sb.append(this.width);
        sb.append(this.height);
        
        this.id = sb.toString().hashCode();
    }
    
    public TiledWebMapServer getServer() {
		return server;
	}

	public void setServer(TiledWebMapServer server) {
		this.server = server;
	}    

}
=======
/**
 * Copyright (C) 2009-2013 BIMserver.org
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.bimserver.models.ifc2x3tc1;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Ifc General Profile Properties</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.bimserver.models.ifc2x3tc1.IfcGeneralProfileProperties#getPhysicalWeight <em>Physical Weight</em>}</li>
 *   <li>{@link org.bimserver.models.ifc2x3tc1.IfcGeneralProfileProperties#getPhysicalWeightAsString <em>Physical Weight As String</em>}</li>
 *   <li>{@link org.bimserver.models.ifc2x3tc1.IfcGeneralProfileProperties#getPerimeter <em>Perimeter</em>}</li>
 *   <li>{@link org.bimserver.models.ifc2x3tc1.IfcGeneralProfileProperties#getPerimeterAsString <em>Perimeter As String</em>}</li>
 *   <li>{@link org.bimserver.models.ifc2x3tc1.IfcGeneralProfileProperties#getMinimumPlateThickness <em>Minimum Plate Thickness</em>}</li>
 *   <li>{@link org.bimserver.models.ifc2x3tc1.IfcGeneralProfileProperties#getMinimumPlateThicknessAsString <em>Minimum Plate Thickness As String</em>}</li>
 *   <li>{@link org.bimserver.models.ifc2x3tc1.IfcGeneralProfileProperties#getMaximumPlateThickness <em>Maximum Plate Thickness</em>}</li>
 *   <li>{@link org.bimserver.models.ifc2x3tc1.IfcGeneralProfileProperties#getMaximumPlateThicknessAsString <em>Maximum Plate Thickness As String</em>}</li>
 *   <li>{@link org.bimserver.models.ifc2x3tc1.IfcGeneralProfileProperties#getCrossSectionArea <em>Cross Section Area</em>}</li>
 *   <li>{@link org.bimserver.models.ifc2x3tc1.IfcGeneralProfileProperties#getCrossSectionAreaAsString <em>Cross Section Area As String</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package#getIfcGeneralProfileProperties()
 * @model
 * @generated
 */
public interface IfcGeneralProfileProperties extends IfcProfileProperties {
	/**
	 * Returns the value of the '<em><b>Physical Weight</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Physical Weight</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Physical Weight</em>' attribute.
	 * @see #isSetPhysicalWeight()
	 * @see #unsetPhysicalWeight()
	 * @see #setPhysicalWeight(double)
	 * @see org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package#getIfcGeneralProfileProperties_PhysicalWeight()
	 * @model unsettable="true"
	 * @generated
	 */
	double getPhysicalWeight();

	/**
	 * Sets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcGeneralProfileProperties#getPhysicalWeight <em>Physical Weight</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Physical Weight</em>' attribute.
	 * @see #isSetPhysicalWeight()
	 * @see #unsetPhysicalWeight()
	 * @see #getPhysicalWeight()
	 * @generated
	 */
	void setPhysicalWeight(double value);

	/**
	 * Unsets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcGeneralProfileProperties#getPhysicalWeight <em>Physical Weight</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetPhysicalWeight()
	 * @see #getPhysicalWeight()
	 * @see #setPhysicalWeight(double)
	 * @generated
	 */
	void unsetPhysicalWeight();

	/**
	 * Returns whether the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcGeneralProfileProperties#getPhysicalWeight <em>Physical Weight</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Physical Weight</em>' attribute is set.
	 * @see #unsetPhysicalWeight()
	 * @see #getPhysicalWeight()
	 * @see #setPhysicalWeight(double)
	 * @generated
	 */
	boolean isSetPhysicalWeight();

	/**
	 * Returns the value of the '<em><b>Physical Weight As String</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Physical Weight As String</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Physical Weight As String</em>' attribute.
	 * @see #isSetPhysicalWeightAsString()
	 * @see #unsetPhysicalWeightAsString()
	 * @see #setPhysicalWeightAsString(String)
	 * @see org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package#getIfcGeneralProfileProperties_PhysicalWeightAsString()
	 * @model unsettable="true"
	 * @generated
	 */
	String getPhysicalWeightAsString();

	/**
	 * Sets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcGeneralProfileProperties#getPhysicalWeightAsString <em>Physical Weight As String</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Physical Weight As String</em>' attribute.
	 * @see #isSetPhysicalWeightAsString()
	 * @see #unsetPhysicalWeightAsString()
	 * @see #getPhysicalWeightAsString()
	 * @generated
	 */
	void setPhysicalWeightAsString(String value);

	/**
	 * Unsets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcGeneralProfileProperties#getPhysicalWeightAsString <em>Physical Weight As String</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetPhysicalWeightAsString()
	 * @see #getPhysicalWeightAsString()
	 * @see #setPhysicalWeightAsString(String)
	 * @generated
	 */
	void unsetPhysicalWeightAsString();

	/**
	 * Returns whether the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcGeneralProfileProperties#getPhysicalWeightAsString <em>Physical Weight As String</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Physical Weight As String</em>' attribute is set.
	 * @see #unsetPhysicalWeightAsString()
	 * @see #getPhysicalWeightAsString()
	 * @see #setPhysicalWeightAsString(String)
	 * @generated
	 */
	boolean isSetPhysicalWeightAsString();

	/**
	 * Returns the value of the '<em><b>Perimeter</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Perimeter</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Perimeter</em>' attribute.
	 * @see #isSetPerimeter()
	 * @see #unsetPerimeter()
	 * @see #setPerimeter(double)
	 * @see org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package#getIfcGeneralProfileProperties_Perimeter()
	 * @model unsettable="true"
	 * @generated
	 */
	double getPerimeter();

	/**
	 * Sets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcGeneralProfileProperties#getPerimeter <em>Perimeter</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Perimeter</em>' attribute.
	 * @see #isSetPerimeter()
	 * @see #unsetPerimeter()
	 * @see #getPerimeter()
	 * @generated
	 */
	void setPerimeter(double value);

	/**
	 * Unsets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcGeneralProfileProperties#getPerimeter <em>Perimeter</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetPerimeter()
	 * @see #getPerimeter()
	 * @see #setPerimeter(double)
	 * @generated
	 */
	void unsetPerimeter();

	/**
	 * Returns whether the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcGeneralProfileProperties#getPerimeter <em>Perimeter</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Perimeter</em>' attribute is set.
	 * @see #unsetPerimeter()
	 * @see #getPerimeter()
	 * @see #setPerimeter(double)
	 * @generated
	 */
	boolean isSetPerimeter();

	/**
	 * Returns the value of the '<em><b>Perimeter As String</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Perimeter As String</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Perimeter As String</em>' attribute.
	 * @see #isSetPerimeterAsString()
	 * @see #unsetPerimeterAsString()
	 * @see #setPerimeterAsString(String)
	 * @see org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package#getIfcGeneralProfileProperties_PerimeterAsString()
	 * @model unsettable="true"
	 * @generated
	 */
	String getPerimeterAsString();

	/**
	 * Sets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcGeneralProfileProperties#getPerimeterAsString <em>Perimeter As String</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Perimeter As String</em>' attribute.
	 * @see #isSetPerimeterAsString()
	 * @see #unsetPerimeterAsString()
	 * @see #getPerimeterAsString()
	 * @generated
	 */
	void setPerimeterAsString(String value);

	/**
	 * Unsets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcGeneralProfileProperties#getPerimeterAsString <em>Perimeter As String</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetPerimeterAsString()
	 * @see #getPerimeterAsString()
	 * @see #setPerimeterAsString(String)
	 * @generated
	 */
	void unsetPerimeterAsString();

	/**
	 * Returns whether the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcGeneralProfileProperties#getPerimeterAsString <em>Perimeter As String</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Perimeter As String</em>' attribute is set.
	 * @see #unsetPerimeterAsString()
	 * @see #getPerimeterAsString()
	 * @see #setPerimeterAsString(String)
	 * @generated
	 */
	boolean isSetPerimeterAsString();

	/**
	 * Returns the value of the '<em><b>Minimum Plate Thickness</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Minimum Plate Thickness</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Minimum Plate Thickness</em>' attribute.
	 * @see #isSetMinimumPlateThickness()
	 * @see #unsetMinimumPlateThickness()
	 * @see #setMinimumPlateThickness(double)
	 * @see org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package#getIfcGeneralProfileProperties_MinimumPlateThickness()
	 * @model unsettable="true"
	 * @generated
	 */
	double getMinimumPlateThickness();

	/**
	 * Sets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcGeneralProfileProperties#getMinimumPlateThickness <em>Minimum Plate Thickness</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Minimum Plate Thickness</em>' attribute.
	 * @see #isSetMinimumPlateThickness()
	 * @see #unsetMinimumPlateThickness()
	 * @see #getMinimumPlateThickness()
	 * @generated
	 */
	void setMinimumPlateThickness(double value);

	/**
	 * Unsets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcGeneralProfileProperties#getMinimumPlateThickness <em>Minimum Plate Thickness</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetMinimumPlateThickness()
	 * @see #getMinimumPlateThickness()
	 * @see #setMinimumPlateThickness(double)
	 * @generated
	 */
	void unsetMinimumPlateThickness();

	/**
	 * Returns whether the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcGeneralProfileProperties#getMinimumPlateThickness <em>Minimum Plate Thickness</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Minimum Plate Thickness</em>' attribute is set.
	 * @see #unsetMinimumPlateThickness()
	 * @see #getMinimumPlateThickness()
	 * @see #setMinimumPlateThickness(double)
	 * @generated
	 */
	boolean isSetMinimumPlateThickness();

	/**
	 * Returns the value of the '<em><b>Minimum Plate Thickness As String</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Minimum Plate Thickness As String</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Minimum Plate Thickness As String</em>' attribute.
	 * @see #isSetMinimumPlateThicknessAsString()
	 * @see #unsetMinimumPlateThicknessAsString()
	 * @see #setMinimumPlateThicknessAsString(String)
	 * @see org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package#getIfcGeneralProfileProperties_MinimumPlateThicknessAsString()
	 * @model unsettable="true"
	 * @generated
	 */
	String getMinimumPlateThicknessAsString();

	/**
	 * Sets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcGeneralProfileProperties#getMinimumPlateThicknessAsString <em>Minimum Plate Thickness As String</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Minimum Plate Thickness As String</em>' attribute.
	 * @see #isSetMinimumPlateThicknessAsString()
	 * @see #unsetMinimumPlateThicknessAsString()
	 * @see #getMinimumPlateThicknessAsString()
	 * @generated
	 */
	void setMinimumPlateThicknessAsString(String value);

	/**
	 * Unsets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcGeneralProfileProperties#getMinimumPlateThicknessAsString <em>Minimum Plate Thickness As String</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetMinimumPlateThicknessAsString()
	 * @see #getMinimumPlateThicknessAsString()
	 * @see #setMinimumPlateThicknessAsString(String)
	 * @generated
	 */
	void unsetMinimumPlateThicknessAsString();

	/**
	 * Returns whether the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcGeneralProfileProperties#getMinimumPlateThicknessAsString <em>Minimum Plate Thickness As String</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Minimum Plate Thickness As String</em>' attribute is set.
	 * @see #unsetMinimumPlateThicknessAsString()
	 * @see #getMinimumPlateThicknessAsString()
	 * @see #setMinimumPlateThicknessAsString(String)
	 * @generated
	 */
	boolean isSetMinimumPlateThicknessAsString();

	/**
	 * Returns the value of the '<em><b>Maximum Plate Thickness</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Maximum Plate Thickness</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Maximum Plate Thickness</em>' attribute.
	 * @see #isSetMaximumPlateThickness()
	 * @see #unsetMaximumPlateThickness()
	 * @see #setMaximumPlateThickness(double)
	 * @see org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package#getIfcGeneralProfileProperties_MaximumPlateThickness()
	 * @model unsettable="true"
	 * @generated
	 */
	double getMaximumPlateThickness();

	/**
	 * Sets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcGeneralProfileProperties#getMaximumPlateThickness <em>Maximum Plate Thickness</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Maximum Plate Thickness</em>' attribute.
	 * @see #isSetMaximumPlateThickness()
	 * @see #unsetMaximumPlateThickness()
	 * @see #getMaximumPlateThickness()
	 * @generated
	 */
	void setMaximumPlateThickness(double value);

	/**
	 * Unsets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcGeneralProfileProperties#getMaximumPlateThickness <em>Maximum Plate Thickness</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetMaximumPlateThickness()
	 * @see #getMaximumPlateThickness()
	 * @see #setMaximumPlateThickness(double)
	 * @generated
	 */
	void unsetMaximumPlateThickness();

	/**
	 * Returns whether the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcGeneralProfileProperties#getMaximumPlateThickness <em>Maximum Plate Thickness</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Maximum Plate Thickness</em>' attribute is set.
	 * @see #unsetMaximumPlateThickness()
	 * @see #getMaximumPlateThickness()
	 * @see #setMaximumPlateThickness(double)
	 * @generated
	 */
	boolean isSetMaximumPlateThickness();

	/**
	 * Returns the value of the '<em><b>Maximum Plate Thickness As String</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Maximum Plate Thickness As String</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Maximum Plate Thickness As String</em>' attribute.
	 * @see #isSetMaximumPlateThicknessAsString()
	 * @see #unsetMaximumPlateThicknessAsString()
	 * @see #setMaximumPlateThicknessAsString(String)
	 * @see org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package#getIfcGeneralProfileProperties_MaximumPlateThicknessAsString()
	 * @model unsettable="true"
	 * @generated
	 */
	String getMaximumPlateThicknessAsString();

	/**
	 * Sets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcGeneralProfileProperties#getMaximumPlateThicknessAsString <em>Maximum Plate Thickness As String</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Maximum Plate Thickness As String</em>' attribute.
	 * @see #isSetMaximumPlateThicknessAsString()
	 * @see #unsetMaximumPlateThicknessAsString()
	 * @see #getMaximumPlateThicknessAsString()
	 * @generated
	 */
	void setMaximumPlateThicknessAsString(String value);

	/**
	 * Unsets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcGeneralProfileProperties#getMaximumPlateThicknessAsString <em>Maximum Plate Thickness As String</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetMaximumPlateThicknessAsString()
	 * @see #getMaximumPlateThicknessAsString()
	 * @see #setMaximumPlateThicknessAsString(String)
	 * @generated
	 */
	void unsetMaximumPlateThicknessAsString();

	/**
	 * Returns whether the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcGeneralProfileProperties#getMaximumPlateThicknessAsString <em>Maximum Plate Thickness As String</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Maximum Plate Thickness As String</em>' attribute is set.
	 * @see #unsetMaximumPlateThicknessAsString()
	 * @see #getMaximumPlateThicknessAsString()
	 * @see #setMaximumPlateThicknessAsString(String)
	 * @generated
	 */
	boolean isSetMaximumPlateThicknessAsString();

	/**
	 * Returns the value of the '<em><b>Cross Section Area</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Cross Section Area</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Cross Section Area</em>' attribute.
	 * @see #isSetCrossSectionArea()
	 * @see #unsetCrossSectionArea()
	 * @see #setCrossSectionArea(double)
	 * @see org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package#getIfcGeneralProfileProperties_CrossSectionArea()
	 * @model unsettable="true"
	 * @generated
	 */
	double getCrossSectionArea();

	/**
	 * Sets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcGeneralProfileProperties#getCrossSectionArea <em>Cross Section Area</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Cross Section Area</em>' attribute.
	 * @see #isSetCrossSectionArea()
	 * @see #unsetCrossSectionArea()
	 * @see #getCrossSectionArea()
	 * @generated
	 */
	void setCrossSectionArea(double value);

	/**
	 * Unsets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcGeneralProfileProperties#getCrossSectionArea <em>Cross Section Area</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetCrossSectionArea()
	 * @see #getCrossSectionArea()
	 * @see #setCrossSectionArea(double)
	 * @generated
	 */
	void unsetCrossSectionArea();

	/**
	 * Returns whether the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcGeneralProfileProperties#getCrossSectionArea <em>Cross Section Area</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Cross Section Area</em>' attribute is set.
	 * @see #unsetCrossSectionArea()
	 * @see #getCrossSectionArea()
	 * @see #setCrossSectionArea(double)
	 * @generated
	 */
	boolean isSetCrossSectionArea();

	/**
	 * Returns the value of the '<em><b>Cross Section Area As String</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Cross Section Area As String</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Cross Section Area As String</em>' attribute.
	 * @see #isSetCrossSectionAreaAsString()
	 * @see #unsetCrossSectionAreaAsString()
	 * @see #setCrossSectionAreaAsString(String)
	 * @see org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package#getIfcGeneralProfileProperties_CrossSectionAreaAsString()
	 * @model unsettable="true"
	 * @generated
	 */
	String getCrossSectionAreaAsString();

	/**
	 * Sets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcGeneralProfileProperties#getCrossSectionAreaAsString <em>Cross Section Area As String</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Cross Section Area As String</em>' attribute.
	 * @see #isSetCrossSectionAreaAsString()
	 * @see #unsetCrossSectionAreaAsString()
	 * @see #getCrossSectionAreaAsString()
	 * @generated
	 */
	void setCrossSectionAreaAsString(String value);

	/**
	 * Unsets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcGeneralProfileProperties#getCrossSectionAreaAsString <em>Cross Section Area As String</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetCrossSectionAreaAsString()
	 * @see #getCrossSectionAreaAsString()
	 * @see #setCrossSectionAreaAsString(String)
	 * @generated
	 */
	void unsetCrossSectionAreaAsString();

	/**
	 * Returns whether the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcGeneralProfileProperties#getCrossSectionAreaAsString <em>Cross Section Area As String</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Cross Section Area As String</em>' attribute is set.
	 * @see #unsetCrossSectionAreaAsString()
	 * @see #getCrossSectionAreaAsString()
	 * @see #setCrossSectionAreaAsString(String)
	 * @generated
	 */
	boolean isSetCrossSectionAreaAsString();

} // IfcGeneralProfileProperties
>>>>>>> 76aa07461566a5976980e6696204781271955163

