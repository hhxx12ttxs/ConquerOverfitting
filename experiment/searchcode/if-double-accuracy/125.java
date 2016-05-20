package stream.series;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

import stream.AbstractProcessor;
import stream.data.Data;
import stream.series.util.EuclideanDistance;
import stream.series.util.NumericValueCaster;
import stream.series.util.NumericValueTransformator;
import stream.series.util.OrderLine;
import stream.series.util.SimpleMotifData;
import stream.series.util.SingleSourceData;
import stream.series.util.SlidingWindow;

public class ExtractMotifs extends AbstractProcessor {

	/* identifiers for fields in SingleSourceData */
	private static String COUNT = "counts";
	private static String ACTIVE_IDS = "active_ids";
	private static String WINDOW = "window";
	private static String ORDER_LINES = "order_lines";
	/* ------------------------------------------ */

	/* --------optional parameters------------ */
	private String sensorId = "";
	private Integer numberOfReferenceVectors = 1;
	private Integer accuracy = 2;
	private Double maxDistance = Double.MAX_VALUE;
	private String motifPrefix = "";
	private Double[] rangeOfValues = { -25d, 25d };
	/* ---------------------------------------- */

	/* ---------mandatory parameters----------- */
	private String[] keys;
	private Integer windowSize;
	private Integer dimensionality;
	/* ---------------------------------------- */

	private HashMap<String, SingleSourceData> sensors;

	public ExtractMotifs() {

		super();
		this.sensors = new HashMap<String, SingleSourceData>();
	}

	public String[] getKeys() {
		return keys;
	}

	public void setKeys(String[] keys) {
		this.keys = keys;
	}

	public String getSensorId() {
		return sensorId;
	}

	public void setSensorId(String sensorId) {
		this.sensorId = sensorId;
	}

	public Integer getWindowSize() {
		return windowSize;
	}

	public void setWindowSize(Integer windowSize) {
		this.windowSize = windowSize;
	}

	public Integer getNumberOfReferenceVectors() {
		return numberOfReferenceVectors;
	}

	public void setNumberOfReferenceVectors(Integer numberOfReferenceVectors) {
		this.numberOfReferenceVectors = numberOfReferenceVectors;
	}

	public Integer getDimensionality() {
		return dimensionality;
	}

	public void setDimensionality(Integer dimensionality) {
		this.dimensionality = dimensionality;
	}

	public Integer getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(Integer accuracy) {
		this.accuracy = accuracy;
	}

	public Double[] getRangeOfValues() {
		return rangeOfValues;
	}

	public void setRangeOfValues(Double[] rangeOfValues) {
		this.rangeOfValues = rangeOfValues;
	}

	public Double getMaxDistance() {
		return maxDistance;
	}

	public void setMaxDistance(Double maxDistance) {
		this.maxDistance = maxDistance;
	}

	@Override
	public void finish() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public Data process(Data data) {

		/*
		 * sensors should be distinguished and there is no 'sensorId' present ->
		 * discard 'data'
		 */
		if (this.sensorId != "" && !data.containsKey(this.sensorId))
			return data;

		/* either 'sensorId' is present or sensors shouldn't be distinguished */
		String sensorId = "none";
		if (data.containsKey(this.sensorId) && this.sensorId != "")
			sensorId = (String) data.get(this.sensorId);

		if (!this.sensors.containsKey(sensorId))
			this.seenNewSensor(sensorId);

		for (String key : this.getKeys()) {

			if (data.containsKey(key)) {

				/* remove oldest element and get its best fit */
				SimpleMotifData motif = this.cleanUp(sensorId, key);
				if (motif != null) {

					/* there is a new motif candidate that must be processed */
					Vector<Double> out = new Vector<Double>();
					for (Double element : motif.getValues())
						out.add(NumericValueTransformator.roundDouble(element,
								this.accuracy));

					data.put(this.motifPrefix + key, out);
				}

				/*
				 * extracting the new vector and inserting it into the order
				 * line(s)
				 */
				Vector<Vector<Double>> values = this.castVector(data.get(key));
				if (!values.isEmpty())
					this.insertElement(sensorId, key, values);
			}
		}
		return data;
	}

	@SuppressWarnings("unchecked")
	private Vector<Vector<Double>> castVector(Serializable values) {

		Vector<Vector<Double>> vector = new Vector<Vector<Double>>();
		try {
			vector.add((Vector<Double>) values);
			return vector;
		} catch (ClassCastException e) {

		}

		try {
			return ((Vector<Vector<Double>>) values);
		} catch (ClassCastException e) {

		}
		return vector;
	}

	@SuppressWarnings("unchecked")
	private void insertElement(String sensor, String key,
			Vector<Vector<Double>> values) {

		DataStructure newElement = this.createNewElement(sensor, key, values);

		if (newElement != null) {
			/* place new element on the order lines */
			Vector<OrderLine> orderLines = ((HashMap<String, Vector<OrderLine>>) this.sensors
					.get(sensor).getDataForAttribute(ORDER_LINES)).get(key);
			Vector<Double> allVals = new Vector<Double>();
			for (Vector<Double> vals : newElement.getValues()) {

				for (Double value : vals) {

					if (this.accuracy > 0)
						allVals.add(NumericValueTransformator.roundDouble(
								value, this.accuracy));
					else
						allVals.add(value);
				}
			}
			for (OrderLine ol : orderLines)
				ol.insertElement(newElement.getId(), allVals);

			/* maintaining a window for each single attribute of each sensor */
			((SlidingWindow) this.sensors.get(sensor).getDataForAttribute(
					WINDOW)).updateWindow(key, newElement.getId());
			/*
			 * activeIds holds all elements that are either in the window or
			 * still someone active element's closest neighbor
			 */
			((HashMap<String, HashMap<Long, DataStructure>>) this.sensors.get(
					sensor).getDataForAttribute(ACTIVE_IDS)).get(key).put(
					newElement.getId(), newElement);

			this.getNearestWindowNeighbor(sensor, key, newElement.getId());
		}
	}

	@SuppressWarnings("unchecked")
	private void getNearestWindowNeighbor(String sensor, String key, Long id) {

		/* candidates for a neighborhood must be generated */
		Vector<Serializable> candidates = new Vector<Serializable>();
		HashMap<String, Vector<OrderLine>> orderLines = (HashMap<String, Vector<OrderLine>>) this.sensors
				.get(sensor).getDataForAttribute(ORDER_LINES);
		HashMap<String, HashMap<Long, DataStructure>> activeIds = (HashMap<String, HashMap<Long, DataStructure>>) this.sensors
				.get(sensor).getDataForAttribute(ACTIVE_IDS);

		for (OrderLine ol : orderLines.get(key)) {

			if (candidates.isEmpty())
				candidates.addAll(ol.getNeighborhoodCandidates(id,
						this.maxDistance));
			else
				candidates.retainAll(ol.getNeighborhoodCandidates(id,
						this.maxDistance));
		}
		DataStructure thisElement = activeIds.get(key).get(id);

		/*
		 * now we have to check for each candidate whether it is a true neighbor
		 * and which one is the best fit
		 */
		Double minDistance = Double.MAX_VALUE;
		Long bestId = -1904L;

		for (Serializable candidate : candidates) {

			DataStructure candidateElement = activeIds.get(key).get(
					(Long) candidate);
			Double distance = this.getDistance(thisElement.getValues(),
					candidateElement.getValues());

			if (distance < this.maxDistance) {

				/* current candidate is a better fit than the last one */
				if (minDistance > distance) {
					minDistance = distance;
					bestId = (Long) candidate;
				}

				/*
				 * all real neighbors must be informed: if this element is a
				 * better fit, than update the candidates best fit
				 */
				if (candidateElement.isNewBestFit(distance)) {

					if (candidateElement.getBestFit() != null) {
						this.decrementLives(sensor, key,
								candidateElement.getBestFit()); // decrements
																// the current
																// best fit's
																// lives by one
					}
					thisElement.becameBestFit(); // increments this element's
													// lives by one
					candidateElement.updateBestFit(id, distance);
				}
			}
		}
		if (bestId >= 0) {
			thisElement.updateBestFit(bestId, minDistance);
			activeIds.get(key).get(bestId).becameBestFit(); // increments best
															// fit's lives by
															// one
		}
	}

	/**
	 * (currently) concatenates all Vector<Vector<Double>> elements in a single
	 * Vector<Double> and computes the euclidean distance between them.
	 * 
	 * @param vector_a
	 * @param vector_b
	 * @return
	 */
	private Double getDistance(Vector<Vector<Double>> vector_a,
			Vector<Vector<Double>> vector_b) {

		if (vector_a.size() != vector_b.size())
			return Double.NaN;

		Vector<Double> a = new Vector<Double>();
		Vector<Double> b = new Vector<Double>();

		for (int i = 0; i < vector_a.size(); i++) {

			if (vector_a.get(i).size() != vector_b.get(i).size())
				return Double.NaN;

			for (int j = 0; j < vector_a.get(i).size(); j++) {

				a.add(vector_a.get(i).get(j));
				b.add(vector_b.get(i).get(j));
			}
		}

		if (this.accuracy > 0)
			return (new EuclideanDistance(this.accuracy)).getDistance(a, b);
		else
			return (new EuclideanDistance()).getDistance(a, b);
	}

	/**
	 * a live is decremented each time, the it is released as a nearest neighbor
	 * (either the other element found a neighbor more close or it simply left
	 * the window). it gets also decreased if the element itself leaves the
	 * window.
	 * 
	 * @param sensor
	 * @param key
	 * @param id
	 */
	private void decrementLives(String sensor, String key, Long id) {

		@SuppressWarnings("unchecked")
		HashMap<String, HashMap<Long, DataStructure>> activeIds = (HashMap<String, HashMap<Long, DataStructure>>) this.sensors
				.get(sensor).getDataForAttribute(ACTIVE_IDS);

		if (activeIds.get(key).get(id).died())
			activeIds.get(key).remove(id);

		this.sensors.get(sensor).putDataForAttribute(ACTIVE_IDS, activeIds);
	}

	/**
	 * creates a new DataStructure element for given attributes.
	 * 
	 * @param sensor
	 * @param key
	 * @param values
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private DataStructure createNewElement(String sensor, String key,
			Vector<Vector<Double>> values) {

		try {

			if (this.accuracy > 0) {

				for (Vector<Double> dim : values) {

					for (Double element : dim)
						element = NumericValueTransformator.roundDouble(
								element, this.accuracy);
				}
			}
			Long id = ((HashMap<String, Long>) this.sensors.get(sensor)
					.getDataForAttribute(COUNT)).get(key);
			DataStructure newElement = new DataStructure(id, values);
			((HashMap<String, Long>) this.sensors.get(sensor)
					.getDataForAttribute(COUNT)).put(key, id + 1);

			return newElement;
		} catch (ClassCastException e) {

			return null;
		}
	}

	/**
	 * creates a new SingleSourceData object with all maps
	 * 
	 * @param sensor
	 */
	private void seenNewSensor(String sensor) {

		SingleSourceData ssd = new SingleSourceData(sensor);
		ssd.putDataForAttribute(COUNT, new HashMap<String, Long>());
		ssd.putDataForAttribute(ACTIVE_IDS,
				new HashMap<String, HashMap<Long, DataStructure>>());
		ssd.putDataForAttribute(WINDOW, new SlidingWindow(this.windowSize));
		ssd.putDataForAttribute(ORDER_LINES, this.createOrderLineObjects());
		this.sensors.put(sensor, ssd);

		for (String key : this.getKeys())
			this.seenNewKey(sensor, key);
	}

	/**
	 * creates all maps for a single attribute in a specified sensor.
	 * 
	 * @param sensor
	 * @param key
	 */
	@SuppressWarnings("unchecked")
	private void seenNewKey(String sensor, String key) {

		HashMap<String, Long> counts = (HashMap<String, Long>) this.sensors
				.get(sensor).getDataForAttribute(COUNT);
		counts.put(key, 0L);
		this.sensors.get(sensor).putDataForAttribute(COUNT, counts);

		HashMap<String, HashMap<Long, DataStructure>> activeIds = (HashMap<String, HashMap<Long, DataStructure>>) this.sensors
				.get(sensor).getDataForAttribute(ACTIVE_IDS);
		activeIds.put(key, new HashMap<Long, DataStructure>());
		this.sensors.get(sensor).putDataForAttribute(ACTIVE_IDS, activeIds);
	}

	/**
	 * creates a new set of {@code OrderLine}.
	 * 
	 * @param key
	 */
	private HashMap<String, Vector<OrderLine>> createOrderLineObjects() {

		HashMap<String, Vector<OrderLine>> allOrderLines = new HashMap<String, Vector<OrderLine>>();

		for (String key : this.getKeys()) {
			Vector<OrderLine> orderLines = new Vector<OrderLine>();
			for (int i = 0; i < this.numberOfReferenceVectors; i++) {

				OrderLine ol = new OrderLine(this.dimensionality,
						this.createReferenceVector());
				ol.setDecimalPlaces(this.accuracy);

				orderLines.add(ol);
			}
			allOrderLines.put(key, orderLines);
		}
		return allOrderLines;
	}

	/**
	 * randomly determines a reference vector with values in
	 * {@link #rangeOfValues}.
	 * 
	 * @return
	 */
	private Vector<Double> createReferenceVector() {

		Vector<Double> reference = new Vector<Double>();
		for (int i = 0; i < this.dimensionality; i++) {

			// [0;1] * (upperBound - lowerBound) + lowerBound
			Double newOrdinate = ((new Random()).nextDouble() * (this.rangeOfValues[1] - this.rangeOfValues[0]))
					+ this.rangeOfValues[0];
			newOrdinate = NumericValueTransformator.roundDouble(newOrdinate,
					this.accuracy);
			reference.add(newOrdinate);
		}
		return reference;
	}

	/**
	 * checks if an element must be removed from the window. If true, it removes
	 * this element from each data structure.
	 * 
	 * @param key
	 */
	@SuppressWarnings("unchecked")
	private SimpleMotifData cleanUp(String sensor, String key) {

		SimpleMotifData motif = null;
		SlidingWindow window = (SlidingWindow) this.sensors.get(sensor)
				.getDataForAttribute(WINDOW);
		HashMap<String, HashMap<Long, DataStructure>> activeIds = (HashMap<String, HashMap<Long, DataStructure>>) this.sensors
				.get(sensor).getDataForAttribute(ACTIVE_IDS);
		HashMap<String, Vector<OrderLine>> orderLines = (HashMap<String, Vector<OrderLine>>) this.sensors
				.get(sensor).getDataForAttribute(ORDER_LINES);
		if (window.isWindowFull(key)) {

			Long id = (Long) window.getWindow(key).get(0);
			Long bestFit = activeIds.get(key).get(id).getBestFit();

			if (bestFit != null) {
				motif = new SimpleMotifData(this.getSingleVector(activeIds
						.get(key).get(bestFit).getValues()));
				/*
				 * decrementing best fit's lives (and removing if no longer
				 * needed)
				 */
				if (activeIds.get(key).get(bestFit).died()) {
					activeIds.get(key).remove(bestFit);
				}
			}
			/*
			 * decrementing leaving element's lives (and removing if no longer
			 * needed)
			 */
			if (activeIds.get(key).get(id).died()) {
				activeIds.get(key).remove(id);
			}
			/* removing leaving element from all order lines */
			for (OrderLine ol : orderLines.get(key)) {
				ol.deleteElement(id);
			}
		}
		this.sensors.get(sensor).putDataForAttribute(WINDOW, window);
		this.sensors.get(sensor).putDataForAttribute(ACTIVE_IDS, activeIds);
		this.sensors.get(sensor).putDataForAttribute(ORDER_LINES, orderLines);

		return motif;
	}

	private Vector<Double> getSingleVector(Vector<Vector<Double>> vector) {

		Vector<Double> singleVector = new Vector<Double>();
		for (Vector<Double> dim : vector) {

			Vector<Double> dimcast = NumericValueCaster.castToDoubleVector(dim);
			for (Double element : dimcast)
				singleVector.add(element);
		}
		return singleVector;
	}

	/**
	 * internal wrapper class
	 * 
	 * @author Markus Kokott - ( markus.kokott(at)udo.edu ) 12.04.2012
	 */
	protected class DataStructure implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5320843683792859395L;
		private Long id; /* this elements id */
		private Vector<Vector<Double>> values; /* this vector */
		/*
		 * set to 1 at time of creation. increments each time, this element
		 * becomes a best fit
		 */
		private Integer lives;

		private Double distanceToBestFit;
		private Long idOfBestFit;

		protected DataStructure(Long id, Vector<Vector<Double>> values) {

			this.id = id;
			this.values = values;
			this.lives = 1;
		}

		public Long getId() {
			return id;
		}

		public Vector<Vector<Double>> getValues() {
			return values;
		}

		public Double getDistanceToBestFit() {
			return distanceToBestFit;
		}

		public Long getIdOfBestFit() {
			return idOfBestFit;
		}

		/**
		 * Call this method if {@link #isNewBestFit(Double)} returns true. It
		 * resets {@link #idOfBestFit} and {@link #distanceToBestFit} to the new
		 * values.
		 * 
		 * @param id
		 * @param distance
		 * @return {@Long} value of old best fit's id (for reducing its
		 *         {@link #lives})
		 */
		protected Long updateBestFit(Long id, Double distance) {

			if (this.idOfBestFit == null) {

				this.idOfBestFit = id;
				this.distanceToBestFit = distance;
				return null;
			}

			if (!this.isNewBestFit(distance)) {
				return null;
			}

			long oldBestFit = this.getBestFit();
			this.idOfBestFit = id;
			this.distanceToBestFit = distance;

			return oldBestFit;
		}

		/**
		 * checks whether the new neighbor is a better fit than the current best
		 * fit.
		 * 
		 * @param distance
		 * @return
		 */
		protected boolean isNewBestFit(Double distance) {

			if (this.idOfBestFit == null) {
				return true;
			}
			return (this.distanceToBestFit > distance);
		}

		/**
		 * increments this elements lives by one, each time it becomes someone's
		 * best fit
		 */
		protected void becameBestFit() {

			this.lives++;
		}

		/**
		 * each time, an element with this element as best fit leaves the window
		 * the 'lives-count' must be decremented. if it reaches zero, the
		 * element is no longer required.
		 * 
		 * @return
		 */
		protected boolean died() {

			this.lives--;
			return (this.lives == 0);
		}

		/**
		 * returns this element's best fit.
		 * 
		 * @return
		 */
		protected Long getBestFit() {

			return this.idOfBestFit;
		}
	}
}

