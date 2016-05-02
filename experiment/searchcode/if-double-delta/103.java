package org.geojme.entities;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.geojme.interfaces.IDataListener;
import org.geojme.interfaces.ITrack;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * A collection of time-stamped locations, together with meta data that
 * describes the overall track
 * 
 * @author ian
 * 
 */
public class VehicleTrack implements ITrack {

	/**
	 * a hint regarding what symbol to use for the track
	 * 
	 */
	private final String _model;
	/**
	 * the history of positions where this vehicle has been
	 * 
	 */
	private final MultiPoint _history;
	/**
	 * the current location of the vehicle (which may be between one of the
	 * history positions)
	 * 
	 */
	private Coordinate _currentLocation;
	private int _currentLocationIndex;
	/**
	 * name of this vehicle
	 * 
	 */
	private final String _name;
	/**
	 * whether this vehicle should be shown
	 * 
	 */
	private boolean _visible;
	/**
	 * Whether to show a current status (course/speed) for this vehicle
	 * 
	 */
	private boolean _showStatus = false;
	private Envelope _myBounds;
	private VehicleState _currentState;
	private static final Logger LOGGER = Logger.getLogger(VehicleTrack.class
			.getName());

	/**
	 * create a new vehicle track
	 * 
	 * @param name
	 *            the name of this vehicle
	 * @param model
	 *            rendering hint for displaying this vehicle
	 * @param history
	 *            route recorded for the vehicle
	 */
	public VehicleTrack(String name, String model, MultiPoint history) {
		_name = name;
		_model = model;
		_history = history;

		// ok, also run through the multi points to get the area of coverage
		Coordinate[] coords = _history.getCoordinates();
		for (final Coordinate coordinate : coords) {
			if (_myBounds == null) {
				_myBounds = new Envelope(coordinate);
			} else {
				_myBounds.expandToInclude(coordinate);
			}
		}

		// initialise the current location, if we can
		if (_history != null) {
			if (!_history.isEmpty()) {
				_currentLocation = startPoint().getCoordinate();
				_currentState = (VehicleState) startPoint().getUserData();
			}
		}
	}

	/**
	 * provide the length of this vehicle
	 * 
	 * @return duh, the length.
	 */
	public float getVehicleLength() {
		return 100;
	}

	@Override
	public String getType() {
		return _model;
	}

	/**
	 * return the current location of the vehicle
	 * 
	 */
	public Coordinate getCurrentLocation() {
		return _currentLocation;
	}

	public Object getData() {
		return null;
	}

	@Override
	public MultiPoint getHistory() {
		return _history;
	}

	@Override
	public long getStartTime() {
		return getState(0).getTime();
	}

	@Override
	public long getEndTime() {
		return getState(_history.getNumGeometries() - 1).getTime();
	}

	private VehicleState getState(int id) {
		Geometry geom = _history.getGeometryN(id);
		Point pt = (Point) geom;
		VehicleState vs = (VehicleState) pt.getUserData();
		return vs;
	}

	@Override
	public String toString() {
		return _name;
	}

	@Override
	public void newTime(long time, IDataListener listener) {
		// ok, are we visible at this time?
		if ((time < getStartTime()) || (time > getEndTime())) {
			// nope, make us non-visible
			setVisible(false);
		}

		// hey, we're alive. calculate the new platform position
		updatePosition(time);

		// tell any listeners about us moving
		listener.entityMoved(this);
	}

	private long getTimeAt(Point p) {
		return ((VehicleState) p.getUserData()).getTime();
	}

	/**
	 * Determine the vehicle location at the specified time
	 * 
	 * @param tgtTime
	 */
	private void updatePosition(long tgtTime) {
		Point lastPos = null, thisPos = null;
		boolean isInterpolate = false;

		if (targetTimeIsBeforeOrSameAsStartPoint(tgtTime)) {
			_currentLocation = startPoint().getCoordinate();
			_currentState = (VehicleState) startPoint().getUserData();
			return;
		} else if (targetTimeAfterOrSameAsEndPoint(tgtTime)) {
			_currentLocation = endPoint().getCoordinate();
			_currentState = (VehicleState) endPoint().getUserData();
			return;
		}

		int trackLen = _history.getNumPoints();
		for (int i = 0; i < trackLen; i++) {
			// retrieve this position
			thisPos = (Point) _history.getGeometryN(i);
			long thisTime = getTimeAt(thisPos);

			if (thisTime == tgtTime) {
				_currentLocation = thisPos.getCoordinate();
				_currentState = (VehicleState) thisPos.getUserData();

				// Store the current index for a future location.
				// Needs to be +1, otherwise there will be no difference
				// between current and future location.
				_currentLocationIndex = i + 1;

				return;
			} // have we passed the target time?
			else if (thisTime >= tgtTime) {
				// we've passed the target time. So the points
				// either side are the ones
				// to interpolate. drop out of the loop
				isInterpolate = true;

				// Store the current index for a future location.
				_currentLocationIndex = i;

				interpolate(lastPos, thisPos, tgtTime);
				break;
			} else {
				// we are still before the target time
				lastPos = thisPos;
				thisPos = null;
			}
		}

		if (lastPos == null && !isInterpolate) {
			// time is before start, result is at thisPos
			_currentLocation = thisPos.getCoordinate();
			_currentState = (VehicleState) thisPos.getUserData();
		} else if (thisPos == null && !isInterpolate) {
			// time is after start, result is at lastPos
			_currentLocation = lastPos.getCoordinate();
			_currentState = (VehicleState) lastPos.getUserData();
		}
	}

	private Point startPoint() {
		return (Point) _history.getGeometryN(0);
	}

	public Coordinate getFutureLocation() {
		return nextPoint().getCoordinate();
	}

	private Point nextPoint() {
		return (_history.getNumPoints() > 1
				&& _currentLocationIndex < _history.getNumPoints() ? (Point) _history
				.getGeometryN(_currentLocationIndex) : null);
	}

	private boolean targetTimeAfterOrSameAsEndPoint(long tgtTime) {
		return tgtTime > getEndTime() || tgtTime == getEndTime();
	}

	private Point endPoint() {
		return (Point) _history.getGeometryN(_history.getNumPoints() - 1);
	}

	private boolean targetTimeIsBeforeOrSameAsStartPoint(long tgtTime) {
		return tgtTime < getStartTime() || tgtTime == getStartTime();
	}

	private void interpolate(Point lastPos, Point thisPos, long tgtTime) {
		LOGGER.log(Level.FINEST, "tgtTime= %s, lastPos = %s, thisPos = %s",
				new Object[] { tgtTime, lastPos, thisPos });

		long delta = tgtTime - getTimeAt(lastPos);
		double changeAmnt = (double) delta
				/ (getTimeAt(thisPos) - getTimeAt(lastPos));

		// result is interpolate between lastPos and thisPos
		_currentLocation = interpolateCoordinates(lastPos, thisPos, delta,
				changeAmnt, tgtTime);

		LOGGER.log(Level.FINEST,
				"interpolated COORDINATE between %s and %s is %s",
				new Object[] { lastPos.getCoordinate(),
						thisPos.getCoordinate(), _currentLocation });

		// VehicleState's _course, _speed and _time
		_currentState = interpolateCourseAndSpeed(lastPos, thisPos, delta,
				changeAmnt, tgtTime);
	}

	private VehicleState interpolateCourseAndSpeed(Point lastPos,
			Point thisPos, float delta, double changeAmnt, long tgtTime) {
		Vector2f lastCourceAndSpeed = new Vector2f(
				(float) getCourseAt(lastPos), (float) getSpeedAt(lastPos));

		Vector2f thisCourceAndSpeed = new Vector2f(
				(float) getCourseAt(thisPos), (float) getSpeedAt(thisPos));

		Vector2f interpolatedVec = new Vector2f();
		interpolatedVec.interpolate(lastCourceAndSpeed, thisCourceAndSpeed,
				(float) changeAmnt);

		LOGGER.log(Level.FINEST, "interpolated SPEED between %s and %s is %s",
				new Object[] { lastCourceAndSpeed, thisCourceAndSpeed,
						interpolatedVec });

		return new VehicleState(tgtTime, (float) getCourseAt(lastPos),
				interpolatedVec.y);
	}

	private Coordinate interpolateCoordinates(Point lastPos, Point thisPos,
			float delta, double changeAmnt, long tgtTime) {
		Vector3f beginVec = new Vector3f((float) lastPos.getCoordinate().x,
				(float) lastPos.getCoordinate().y,
				(float) lastPos.getCoordinate().z);

		Vector3f finalVec = new Vector3f((float) thisPos.getCoordinate().x,
				(float) thisPos.getCoordinate().y,
				(float) thisPos.getCoordinate().z);

		LOGGER.log(
				Level.FINEST,
				"changeAmnt[%s] = delta[%s]/thisPos.time[%s] - lastPos.getTime[%s]",
				new Object[] { changeAmnt, delta, getTimeAt(thisPos),
						getTimeAt(lastPos) });

		Vector3f interpolatedVec = new Vector3f();
		interpolatedVec.interpolate(beginVec, finalVec, (float) changeAmnt);

		return new Coordinate(interpolatedVec.x, interpolatedVec.y,
				interpolatedVec.z);
	}

	private double getCourseAt(Point p) {
		return ((VehicleState) p.getUserData()).getCourse();
	}

	private double getSpeedAt(Point p) {
		return ((VehicleState) p.getUserData()).getSpeed();
	}

	/**
	 * specify visibility of this entity
	 * 
	 * @param b
	 */
	private void setVisible(boolean b) {
		_visible = b;
	}

	public boolean isVisible() {
		return _visible;
	}

	@Override
	public boolean isShowStatus() {
		return _showStatus;
	}

	public void setShowStatus(boolean val, IDataListener listener) {
		_showStatus = val;

		// and fire out indications that it's changed
		listener.entityStyled(this);
	}

	@Override
	public Envelope getBounds() {
		return _myBounds;
	}

	@Override
	public VehicleState getCurrentState() {
		return _currentState;
	}

	public String getCRSName() {
		return "crs:84";
	}
}

