package at.fhj.itm.model;

import org.primefaces.model.map.LatLng;

/**
 *  A point of a calculated route
 * @author Seuchter
 *
 */
public class Point extends LatLng{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8291855900136104222L;
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + duration;
		result = prime * result + id;
		long temp;
		temp = Double.doubleToLongBits(super.getLat());
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(super.getLng());
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + order;
		result = prime * result + tripId;
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Point other = (Point) obj;
		if (duration != other.duration)
			return false;
		if (id != other.id)
			return false;
		if (Double.doubleToLongBits(super.getLat()) != Double.doubleToLongBits(other.getLat()))
			return false;
		if (Double.doubleToLongBits(super.getLng()) != Double.doubleToLongBits(other.getLng()))
			return false;
		if (order != other.order)
			return false;
		if (tripId != other.tripId)
			return false;
		return true;
	}
	private int id;
	private int order;
	private int duration;
	private int tripId;
	/**
	 * Creates a point
	 * @param id
	 * @param order
	 * @param lng
	 * @param lat
	 * @param duration
	 */
	public Point(int id, int order, double lng, double lat, int duration, int tripId) {
		super(lat, lng);
		setId(id);
		setOrder(order);
		setDuration(duration);
		setTripId(tripId);
	}
	/**
	 * Creates a point with a id of -1 which means not persisted.
	 * @param order
	 * @param lng
	 * @param lat
	 * @param duration
	 */
	public Point(int order, double lng, double lat, int duration, int tripId) {
		this(-1,order,lng,lat,duration,tripId);
	}
	
	public final int getId() {
		return id;
	}
	public final void setId(int id) {
		this.id = id;
	}
	public final int getOrder() {
		return order;
	}
	public final void setOrder(int order) {
		this.order = order;
	}
	public final double getLng() {
		return super.getLng();
	}
	public final double getLat() {
		return super.getLat();
	}
	public final int getDuration() {
		return duration;
	}
	public final void setDuration(int duration) {
		this.duration = duration;
	}
	public final int getTripId() {
		return tripId;
	}
	public final void setTripId(int tripId) {
		this.tripId = tripId;
	}
	
	
	
	
}

