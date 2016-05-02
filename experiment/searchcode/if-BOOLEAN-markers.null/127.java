package de.fhpotsdam.unfolding.marker;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.fhpotsdam.unfolding.UnfoldingMap;

/**
 * Manages markers of different types.
 * Is always connected to one map (for location to screen coordinate conversion).
 */
public class MarkerManager<E extends Marker> {

	public static Logger log = Logger.getLogger(MarkerManager.class);

	UnfoldingMap map;
	List<E> markers;
	protected boolean bEnableDrawing;

	public MarkerManager() {
		markers = new ArrayList<E>();
		bEnableDrawing = true;
	}
	
	public MarkerManager(List<E> markers) {
		this();
		addMarkers(markers);
	}

	public void setMap(UnfoldingMap map) {
		this.map = map;
	}
	
	public void setMarkers(List<E> markers) {
		if (markers != null) {
			this.markers = markers;
		}
		else {
			// Convenient method. Users should use clearMarkers() directly.
			clearMarkers();
		}
	}
	
	public boolean removeMarker(E marker) {
		return markers.remove(marker);
	}
	
	public void clearMarkers() {
		markers.clear();
	}
	
	public boolean isDrawingEnabled(){
		return bEnableDrawing;
	}
	
	public void enableDrawing(){
		bEnableDrawing = true;
	}
	
	public void disableDrawing(){
		bEnableDrawing = false;
	}
	
	public void toggleDrawing(){
		bEnableDrawing = !bEnableDrawing;
	}

	public boolean addMarker(E marker) {
		if (markers == null) {
			this.markers = new ArrayList<E>();
		}

		if (markers.contains(marker))
			return false;

		markers.add(marker);
		return true;
	}

	public void addMarkers(List<E> markers) {
		if (this.markers == null) {
			this.markers = new ArrayList<E>();
		}
		this.markers.addAll(markers);
	}

	public List<E> getMarkers() {
		return markers;
	}

	/**
	 * @deprecated Replaced by {@link #getFirstHitMarker(float, float)}
	 */
	@Deprecated
	public Marker isInside(float checkX, float checkY) {
		return getFirstHitMarker(checkX, checkY);
	}

	public E getNearestMarker(float checkX, float checkY) {
		E foundMarker = null;
		double minDist = Double.MAX_VALUE;
		for (E marker : markers) {
			double dist = marker.getDistanceTo( map.getLocation(checkX, checkY) );
			if (minDist == dist){
				if(marker.isInside(map, checkX, checkY)){
					foundMarker = marker;
				}
			}else if (minDist > dist) {
				minDist = dist;
				foundMarker = marker;
			}
		}
		return foundMarker;
	}

	public E getFirstHitMarker(float checkX, float checkY) {
		
		E foundMarker = null;
		// NB: Markers should be ordered, e.g. by size ascending, i.e. big, medium, small
		for (E marker : markers) {

			// NB: If markers are order by size descending, i.e. small, medium, big
			// for (int i = markers.size() - 1; i >= 0; i--) {
			// Marker marker = markers.get(i);

			if (marker.isInside(map, checkX, checkY)) {
				foundMarker = marker;
				break;
			}
		}
		return foundMarker;
	}

	public List<E> getHitMarkers(float checkX, float checkY) {
		List<E> hitMarkers = new ArrayList<E>();
		for (E marker : markers) {
			if (marker.isInside(map, checkX, checkY)) {
				hitMarkers.add(marker);
			}
		}
		return hitMarkers;
	}

	public void draw() {
		if (!bEnableDrawing)
			return;
		
		for (Marker marker : markers) {
			marker.draw(map);
		}
	}
	

}

