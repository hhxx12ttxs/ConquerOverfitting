package stream.series.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

/**
 * This class provides a data structure that can be used to generate candidates
 * for nearest neighbor search in Cartesian spaces. A reference vector is used
 * to receive an lower bound distance for any two elements in the Cartesian
 * space.<br>
 * So it is possible to prune away some elements that can't have a pairwise
 * distance less than a given threshold. This implementation follows
 * <i>Mueen</i> and <i> Keogh's</i> idea of an order line. For more information
 * refer to their paper <i>"Exact Discovery of Time Series Motifs"</i>
 * 
 * @author Markus Kokott ( markus.kokott(at)udo.edu ) 27.02.2012
 */
public class OrderLine implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3526970682454146204L;

	private Integer dimensionality;
	private HashMap<Double, Vector<Serializable>> orderLine;
	private HashMap<Serializable, Double> distances;
	private Vector<Double> referenceVector;
	private Integer decimalPlaces = -1;

	/**
	 * This is the only valid constructor for an {@code OrderLine}-object. It
	 * needs a valid dimensionality (i.e. >0) and a reference vector of that
	 * dimension.
	 * 
	 * @param dimensionality
	 *            - Integer value > 0
	 * @param referenceVector
	 *            - Each later element will be compared to this reference
	 *            vector. Its distance determines the element's position on the
	 *            order line.
	 */
	public OrderLine(Integer dimensionality, Vector<Double> referenceVector) {

		if (referenceVector.isEmpty()
				|| dimensionality != referenceVector.size()) {

			throw new RuntimeException(
					"dimensionality of provided reference vector doesn't fit (or is set to zero).");
		}

		this.dimensionality = dimensionality;
		this.referenceVector = referenceVector;
		this.distances = new HashMap<Serializable, Double>();
		this.orderLine = new HashMap<Double, Vector<Serializable>>();
	}

	public Integer getDecimalPlaces() {
		return decimalPlaces;
	}

	/**
	 * This method specifies the number of decimal places used for accuracy. The
	 * provided value influences the number of "different" distances at the
	 * order line. E.g. a value of 1 means, that distances 0.1234 and 0.100023
	 * are both treated as distance 0.1.
	 * 
	 * @param decimalPlaces
	 */
	public void setDecimalPlaces(Integer decimalPlaces) {
		this.decimalPlaces = decimalPlaces;
	}

	/**
	 * To place a new element on the order line, this method must be called with
	 * the element itself and its Cartesian coordinates. Please note, that an
	 * element mustn't have more than one position in space. If this method is
	 * provided with an already existing element, it will simply ignore the new
	 * element even if its coordinates differ!
	 * 
	 * @param element
	 * @param coordinates
	 */
	public void insertElement(Serializable element, Vector<Double> coordinates) {

		if (!(this.dimensionality == coordinates.size())) {

			throw new RuntimeException(
					"dimensions of provided vector doesn't fit!");
		}

		if ((element != null) && !this.distances.containsKey(element)) {

			Double distance = (new EuclideanDistance()).getDistance(
					this.referenceVector, coordinates);
			this.insertElementPreservingOrder(element, distance);
		}
	}

	/**
	 * Returns all candidates to have a distance less provided
	 * {@code maxDistance}. Candidates are returned pairwise. Please note, that
	 * the real distance have to be computed, because an order line provides
	 * just an lower bound for the distance of two elements. The real distance
	 * can be much greater, but elements that aren't returned as a candidate
	 * pair can't be closer than {@code maxDistance}. This means there will
	 * probably be false positives, but there can't be any false negatives!
	 * 
	 * @param maxDistance
	 * @return
	 */
	public Vector<Serializable[]> getCandidates(Double maxDistance) {

		// get the order line in ascending order
		Double[] distances = { 0d };
		distances = this.orderLine.keySet().toArray(distances);
		Arrays.sort(distances);

		Vector<Serializable[]> candidates = new Vector<Serializable[]>();

		for (int i = 0; i < distances.length; i++) {

			int k = i;
			while (distances[k] - distances[i] <= maxDistance) {

				Vector<Serializable[]> temp = this.getAllPairs(distances[i],
						distances[k]);
				if (!temp.isEmpty()) {

					candidates.addAll(temp);
				}
			}
		}

		return candidates;
	}

	/**
	 * Method removes a given element from the order line if exists. It returns
	 * {@code true} if element got deleted or {@code false} otherwise.
	 * 
	 * @param element
	 * @return
	 */
	public boolean deleteElement(Serializable element) {

		if (this.distances.containsKey(element)) {

			this.orderLine.get(this.distances.get(element)).removeElement(
					element);
			if (this.orderLine.get(this.distances.get(element)).isEmpty()) {

				this.orderLine.remove(this.distances.get(element));
			}
			this.distances.remove(element);
			return true;
		}
		return false;
	}

	/**
	 * To receive all elements that may be in a neighborhood of size
	 * {@code maxDistance} of a given {@code element} call this method. It will
	 * return any elements with a lower bound distance at most
	 * {@code maxDistance} to {@code element}. There may be false positives,
	 * though.
	 * 
	 * @param element
	 * @return
	 */
	public Vector<Serializable> getNeighborhoodCandidates(Serializable element,
			Double maxDistance) {

		if (!this.distances.containsKey(element)) {

			return new Vector<Serializable>();
		}

		Vector<Serializable> candidates = new Vector<Serializable>();
		Double elementsDistance = this.distances.get(element);
		Double lowerBound = elementsDistance - maxDistance;
		Double upperBound = elementsDistance + maxDistance;

		for (Double distance : this.orderLine.keySet()) {

			if ((distance >= lowerBound) && (distance <= upperBound)) {

				for (Serializable thing : this.orderLine.get(distance)) {

					if (!thing.equals(element)) {
						candidates.add(thing);
					}
				}
			}
		}

		return candidates;
	}

	/**
	 * 
	 * @param element
	 * @param distance
	 */
	private void insertElementPreservingOrder(Serializable element,
			Double distance) {

		if (this.decimalPlaces > 0) {

			distance = NumericValueTransformator.roundDouble(distance,
					this.decimalPlaces);
		}
		if (!this.orderLine.containsKey(distance)) {

			this.orderLine.put(distance, new Vector<Serializable>());
		}
		this.distances.put(element, distance);
		this.orderLine.get(distance).add(element);
	}

	/**
	 * Returns all possible pairs of two given distances. If both distances are
	 * equal, it will return all pairs except pairs of equal elements.
	 * 
	 * @param distance1
	 * @param distance2
	 * @return
	 */
	private Vector<Serializable[]> getAllPairs(Double distance1,
			Double distance2) {

		if (!this.orderLine.containsKey(distance1)
				|| !this.orderLine.containsKey(distance2)) {

			return new Vector<Serializable[]>();
		}

		Vector<Serializable[]> pairs = new Vector<Serializable[]>();
		for (Serializable element1 : this.orderLine.get(distance1)) {

			for (Serializable element2 : this.orderLine.get(distance2)) {

				if (!element1.equals(element2)) {

					Serializable[] pair = { element1, element2 };
					pairs.add(pair);
				}
			}
		}

		return pairs;
	}
}

