package sensor.core;

/**
 * A sensor is an interface for a specific sensor, which is designed to send
 * data on specific implemented events. An event
 * 
 * @author ajc
 * 
 */
public abstract class HWSensor extends Thread implements Sensor {

	/** Double encoding for a false boolean state */
	public static final double FALSE = 0;
	/** Double encoding for a true boolean state */
	public static final double TRUE = 1;

	private static final int DEFAULT_NUM_DATA = 1;
	private static final int DEFAULT_POLLTIME = 50;

	// data is stored in an array
	private double[] data;
	// run states
	private boolean running;
	private boolean suspended;
	// time between polls
	private int pollTime;
	// name assigned to particular sensor
	private String name;

	/**
	 * 
	 * 
	 */
	public HWSensor() {
		data = new double[DEFAULT_NUM_DATA];
		running = false;
		suspended = false;
		pollTime = DEFAULT_POLLTIME;
		name = "";
	}

	public void run() {
		running = true;
		while (running) {
			try {
				if (!suspended) {
					poll();
					// notifyDataListeners();
				}
				sleep(pollTime);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * Saves data from hardware calls to sensor state
	 */
	protected abstract void poll();

	/**
	 * Sets sensor state from a hardware call, and notifies self on detected
	 * state change
	 * 
	 * @param key
	 *            storage index of this piece of data
	 * @param value
	 *            numeric data from hardware call
	 */
	protected void setState(int key, double value) {
		if (data.length <= key) {
			// increase length of data by making it a copy of itself with
			// increased length
			// data = Arrays.copy(data, 0, key + 1, 0, data.length)
			// System.a
			data = new double[key + 1];
		}
		// notify self on state change
		if (value != data[key]) {
			sensorStateChanged(key, data[key], value);
		}
		// save new data
		data[key] = value;

	}

	/**
	 * Sets sensor state from a boolean hardware data
	 * 
	 * @param key
	 *            storage index of this piece of data
	 * @param value
	 *            boolean data from hardware call
	 */
	protected void setState(int key, boolean value) {
		setState(key, value ? TRUE : FALSE);
	}

	/**
	 * 
	 * @param key
	 *            storage index for stored piece of data
	 * @return numeric data from previous hardware call stored
	 */
	protected synchronized double getState(int key) {
		if (data.length < key) {
			data = new double[key];
			return ERROR;
		}
		return data[key];
	}

	/**
	 * Adjusts the period of calls to poll():
	 * 
	 * @param pollTime
	 *            time between polls
	 */
	public void setSleepTime(int pollTime) {
		this.pollTime = pollTime;
	}

	/**
	 * Stops polling for data
	 */
	public void stopSensor() {
		running = false;
	}

	/**
	 * 
	 * @return true if sensor is standing by and pausing data polling
	 */
	public boolean isSuspended() {
		return suspended;
	}

	/**
	 * Pauses data acquisition by pausing calls to poll()
	 */
	public void pause() {
		suspended = true;
	}

	/**
	 * Resumes data acquisition by resuming calls to poll()
	 */
	public void unpause() {
		suspended = false;
	}

	/**
	 * Maybe rename to notifyListeners... or onSensorStateChange
	 * 
	 * It also maybe useful to include delta units
	 * 
	 * @param id
	 * @param prevData
	 * @param data
	 */
	protected abstract void sensorStateChanged(int id, double prevData,
			double data);

	public void setID(String name) {
		this.name = name;
	}

	public String getID() {
		return name;
	}

}
