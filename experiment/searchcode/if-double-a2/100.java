// Created by plusminus on 21:28:12 - 25.09.2008
package org.andnav.osm.util;

import java.util.List;


import org.achartengine.model.Point;
import org.andnav.osm.util.constants.GeoConstants;
import org.andnav.osm.views.util.constants.MathConstants;


import android.location.Location;



/**
 *
 * @author Nicolas Gramlich
 *
 */
public class GeoPoint implements MathConstants, GeoConstants{
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private int mLongitudeE6;
	private int mLatitudeE6;

	// ===========================================================
	// Constructors
	// ===========================================================

	public GeoPoint(final int aLatitudeE6, final int aLongitudeE6) {
		this.mLatitudeE6 = aLatitudeE6;
		this.mLongitudeE6 = aLongitudeE6;
	}


	protected static GeoPoint fromDoubleString(final String s, final char spacer) {
		final int spacerPos = s.indexOf(spacer);
		return new GeoPoint((int) (Double.parseDouble(s.substring(0,
				spacerPos - 1)) * 1E6), (int) (Double.parseDouble(s.substring(
				spacerPos + 1, s.length())) * 1E6));
	}

	public static GeoPoint fromDoubleString(final String s){
		//final int commaPos = s.indexOf(',');
		final String[] f = s.split(",");
		return new GeoPoint((int)(Double.parseDouble(f[0])* 1E6),
				(int)(Double.parseDouble(f[1])* 1E6));
		//return new GeoPoint((int)(Double.parseDouble(s.substring(0,commaPos-1))* 1E6),
		//		(int)(Double.parseDouble(s.substring(commaPos+1,s.length()))* 1E6));
	}

	public static GeoPoint from2DoubleString(final String lat, final String lon) {
		try {
			return new GeoPoint((int) (Double.parseDouble(lat) * 1E6),
					(int) (Double.parseDouble(lon) * 1E6));
		} catch (NumberFormatException e) {
			return new GeoPoint(0,0);
		}
	}
	public static Point fromintString(final String lat, final String lon)
	{
		Point point=new Point();
		point.setX((float) (Integer.parseInt(lat)/1E6));
		point.setY((float) (Integer.parseInt(lon)/1E6));
		return point;
		
		
	}

	public static GeoPoint fromIntString(final String s){
		final int commaPos = s.indexOf(',');
		return new GeoPoint(Integer.parseInt(s.substring(0,commaPos-1)),
				Integer.parseInt(s.substring(commaPos+1,s.length())));
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public int getLongitudeE6() {
		return this.mLongitudeE6;
	}

	public int getLatitudeE6() {
		return this.mLatitudeE6;
	}

	public double getLongitude() {
		return this.mLongitudeE6/1E6;
	}

	public double getLatitude() {
		return this.mLatitudeE6/1E6;
	}

	public void setLongitudeE6(final int aLongitudeE6) {
		this.mLongitudeE6 = aLongitudeE6;
	}

	public void setLatitudeE6(final int aLatitudeE6) {
		this.mLatitudeE6 = aLatitudeE6;
	}

	public void setCoordsE6(final int aLatitudeE6, final int aLongitudeE6) {
		this.mLatitudeE6 = aLatitudeE6;
		this.mLongitudeE6 = aLongitudeE6;
	}

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	@Override
	public String toString(){
		return new StringBuilder().append(this.mLatitudeE6).append(",").append(this.mLongitudeE6).toString();
	}

	public String toDoubleString() {
		return new StringBuilder().append(this.mLatitudeE6 / 1E6).append(",").append(this.mLongitudeE6  / 1E6).toString();
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof GeoPoint))
			return false;
		GeoPoint g = (GeoPoint)obj;
		return g.mLatitudeE6 == this.mLatitudeE6 && g.mLongitudeE6 == this.mLongitudeE6;
	}

	// ===========================================================
	// Methods
	// ===========================================================

	/**
	 * @see Source@ http://www.geocities.com/DrChengalva/GPSDistance.html
	 * @param gpA
	 * @param gpB
	 * @return distance in meters
	 */
	public int distanceTo(final GeoPoint other) {

		final double a1 = DEG2RAD * (this.mLatitudeE6 / 1E6);
		final double a2 = DEG2RAD * (this.mLongitudeE6 / 1E6);
		final double b1 = DEG2RAD * (other.mLatitudeE6 / 1E6);
		final double b2 = DEG2RAD * (other.mLongitudeE6 / 1E6);

		final double cosa1 = Math.cos(a1);
		final double cosb1 = Math.cos(b1);

		final double t1 = cosa1*Math.cos(a2)*cosb1*Math.cos(b2);

		final double t2 = cosa1*Math.sin(a2)*cosb1*Math.sin(b2);

		final double t3 = Math.sin(a1)*Math.sin(b1);

		final double tt = Math.acos( t1 + t2 + t3 );

		return (int)(RADIUS_EARTH_METERS*tt);
	}
	public static double distanceTo(final GeoPoint a,final GeoPoint b) {

		final double a1 = DEG2RAD * (a.mLatitudeE6 / 1E6);
		final double a2 = DEG2RAD * (a.mLongitudeE6 / 1E6);
		final double b1 = DEG2RAD * (b.mLatitudeE6 / 1E6);
		final double b2 = DEG2RAD * (b.mLongitudeE6 / 1E6);

		final double cosa1 = Math.cos(a1);
		final double cosb1 = Math.cos(b1);

		final double t1 = cosa1*Math.cos(a2)*cosb1*Math.cos(b2);

		final double t2 = cosa1*Math.sin(a2)*cosb1*Math.sin(b2);

		final double t3 = Math.sin(a1)*Math.sin(b1);

		final double tt = Math.acos( t1 + t2 + t3 );

		return (RADIUS_EARTH_METERS*tt);
	}
	/**
	 * 璁＄畻浼犲叆缁忕含搴﹀潗鏍囦覆鐨勯潰绉紝杩斿洖浠ョ背鍗曚綅鐨勯潰绉�
	 * 
	 * @param list 鍧愭爣鍒楄〃	
	 * @return
	 */
	public static double GetGeoArea(List<GeoPoint> list)
	{	
		if(list.size() < 3)
			return 0;
		
		double xmin, ymin, xmax, ymax;
	    xmin = xmax = list.get(0).getLongitude();
	    ymax = ymin = list.get(0).getLatitude();   	    

	    for (int i=1; i<list.size(); ++i)
	    {
	    	GeoPoint p = list.get(i);
	        xmin = Math.min(xmin, p.getLongitude());
	        ymin = Math.min(ymin, p.getLatitude());
	        xmax = Math.max(xmax, p.getLongitude());
	        ymax = Math.max(ymax, p.getLatitude());
	    }	    
	    
	    //璁＄畻缁忕含搴﹀鎺ョ煩褰㈢殑闈㈢Н
	    double dAreaJW_J = (xmax-xmin)*(ymax-ymin);
	    
	    //璁＄畻缁忕含搴﹀杈瑰舰鐨勯潰绉�
	    double dAreaJW_M = getArea(list);
	    dAreaJW_M = Math.abs(dAreaJW_M);
	    if (dAreaJW_M == 0)
	        return 0;
	    
	    //璁＄畻杞崲涓簃鐨勭煩褰㈤潰绉�
	    float[] dGeoX = {0};
	    float[] dGeoY = {0};   
	    
	    //鍒╃敤android鐨勮窛绂昏绠楀叕寮忓緱鍒板鎺ョ煩褰㈢殑鍦扮悊闀垮
	    Location.distanceBetween(ymin, xmin, ymax, xmin, dGeoX);
	    Location.distanceBetween(ymin, xmin, ymin, xmax, dGeoY);
	    
	    //寰楀埌澶栨帴鐭╁舰鐨勭湡瀹炲湴鐞嗛潰绉�
	    double geoAreaRect = dGeoX[0]*dGeoY[0];	    
	    
	    double dTureArea = geoAreaRect*dAreaJW_M/dAreaJW_J;	    
	    
	    return dTureArea;
	}
	/**
	 * 璁＄畻浼犲叆鍧愭爣涓茬殑闈㈢Н	
	 * 
	 * @param list 鍧愭爣鍒楄〃	
	 * @return
	 */
	public static double getArea(List<GeoPoint> list)
	{
	   //S = 0.5 * ( (x0*y1-x1*y0) + (x1*y2-x2*y1) + ... + (xn*y0-x0*yn) )

	   double area = 0.00;
	   for(int i = 0;i<list.size();i++){
	    if(i<list.size()-1){
	    	GeoPoint p1 = list.get(i);
	    	GeoPoint p2 = list.get(i+1);
	     area += p1.getLongitude()*p2.getLatitude() - p2.getLongitude()*p1.getLatitude();
	    }else{
	    	GeoPoint pn = list.get(i);
	    	GeoPoint p0 = list.get(0);
	     area += pn.getLongitude()*p0.getLatitude()- p0.getLongitude()*pn.getLatitude();
	    }
	   
	   }
	   area = area/2.00;
	  
	   return area;
	}
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}

