package perceptron;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * Candidate objects.
 * 
 * @author Michael Becker
 * @author Christopher Potts
 * @version 2.0 created 2007-02; last update 2007-02-19
 */

public class Candidate {

	/*
	 * ================================================================= Class
	 * variables
	 * =================================================================
	 */

	/** The output form. */
	private LinguisticForm output;

	/** The frequencies. If positive, then this is a winner, else it's a loser. */
	private double frequency;

	/** The list of violations, as a list of Cell objects. */
	private ArrayList<Cell> violationVector;

	/** Specifies which tableau this candidate belongs to. */
	private Tableau parent;

	/*
	 * =================================================================
	 * Constructor
	 * =================================================================
	 */

	/** Construct a new candidate from a Tableau. */
	public Candidate(Tableau tableau) { // public spec was missing; wasn't sure
										// why, so added --- chris
		this.parent = tableau;
	}

	public Candidate() {

	}

	/*
	 * ================================================================= Inputs
	 * and outputs
	 * =================================================================
	 */

	/**
	 * Returns a String representation of the output form.
	 * 
	 * @return this.output A String representing the output form.
	 */
	public String getName() {
		return output.getText();
	}

	/**
	 * Returns a LinguisticForm, a potentially richer representation than just a
	 * string. @see #getName()
	 * 
	 * @return this.output LinguisticForm object representing this form.
	 */
	public LinguisticForm getOutput() {
		return this.output;
	}

	/**
	 * Returns the Tableau that the current Candidate belongs to
	 * 
	 * @return this.parent Tableau object.
	 */
	public Tableau getParent() {
		return this.parent;
	}

	public void setParent(Tableau tab) {
		this.parent = tab;
	}

	/**
	 * Permits setting and changing of the output form.
	 * 
	 * @param output
	 *            The new output LinguisticForm.
	 * @return true
	 */
	public boolean setOutput(LinguisticForm output) {
		this.output = output;
		return true;
	}

	/*
	 * ================================================================= Weights
	 * =================================================================
	 */

	/**
	 * Get the weighted total of the candidate by going over the violation marks
	 * and multiplying each by the weight of the corresponding constraint.
	 * Assumes that the constraints and the violation marks are paired
	 * correctly.
	 * 
	 * @return weight The weight, a double
	 */
	// TODO: This should really be called get getHarmony().
	public Double getWeight() {
		Double weight = 0.0;
		Iterator viol = this.getViolationVector().iterator();
		// System.out.println(this.parent.toString());
		// System.out.println(this.parent.getCon().size());
		Iterator con = this.parent.getCon().iterator();
		while (viol.hasNext() && con.hasNext()) {
			Cell cell = (Cell) viol.next();
			LinguisticConstraint constraint = (LinguisticConstraint) con.next();
			weight += cell.getViolation() * constraint.getWeight();
		}
		return weight;
	}

	/**
	 * Same as getWeight, except that each constraint weight has a normally
	 * distributed number added to it, to simulate evaluation noise. Each number
	 * is pulled from a normal distribution with mean 0 and standard deviation
	 * supplied by the user.
	 * 
	 * @param stdDev
	 *            the standardDeviation
	 * @return weight The weight, a double
	 */

	// Once again, this should be called getHarmonyWithNoise().
	public Double getWeightWithNoise(int stdDev) {
		Double weight = 0.0;
		Iterator viol = this.getViolationVector().iterator();
		Iterator con = this.parent.getCon().iterator();
		while (viol.hasNext() && con.hasNext()) {
			Cell cell = (Cell) viol.next();
			LinguisticConstraint constraint = (LinguisticConstraint) con.next();
			weight += cell.getViolation()
					* (constraint.getWeight() + this.getNoise(stdDev));
		}
		return weight;
	}

	/**
	 * Get a rounded version of the weighted total for this candidate.
	 * 
	 * @return roundedWeight The weight rounded to 3 places.
	 * @see #getWeight()
	 */
	public Double getRoundedWeight() {
		double weight = getWeight();
		DecimalFormat df = new DecimalFormat("0.###");
		String weightString = df.format(weight);
		double roundedWeight = new Double(weightString);
		return roundedWeight;
	}

	/**
	 * Get the specified weight for a particular cell. *. //public Double
	 * getCellWeight(int i) { // double weight = 0.0; // return weight; //}
	 * 
	 * /* =================================================================
	 * Violations
	 * =================================================================
	 */

	/**
	 * Get the violation count for constraint indexed i.
	 * 
	 * @param i
	 *            The index of the constraint whose violation we're getting
	 * @return violations The violation count for the cell in question
	 */
	public double getViolation(int i) {
		Cell cell = getViolationVector().get(i);
		return cell.getViolation();
	}

	/**
	 * Get the full list of Cells.
	 * 
	 * @return this.violationVector An ArrayList of Cells.
	 */
	public ArrayList<Cell> getViolationVector() {
		return this.violationVector;
	}

	/**
	 * Add a violation by adding a cell with a specified value.
	 * 
	 * @param cell
	 *            The cell we're adding a violation to.
	 * @return true
	 */
	public boolean addViolation(Cell cell) {
		if (this.violationVector == null) {
			this.violationVector = new ArrayList<Cell>();
		}
		this.violationVector.add(cell);
		return true;
	}

	/*
	 * =================================================================
	 * Frequencies (winner or loser?)
	 * =================================================================
	 */

	/**
	 * Specify whether a canddiate is a winner or not, by setting its frequency.
	 * 
	 * @param freq
	 *            The frequency of the candidate.
	 * @return true
	 */
	public boolean setFrequency(double freq) {
		this.frequency = freq;
		return true;
	}

	/**
	 * Get the Candidate's frequency (largely to determine whether it is a
	 * winner).
	 * 
	 * @return this.frequency The frequency of the candidate.
	 */
	public double getFrequency() {
		return this.frequency;
	}

	/*
	 * =================================================================
	 * Printing
	 * =================================================================
	 */

	/**
	 * A string representation of the Candidate.
	 * 
	 * @return str The Candidate represented as a string the parent tableau's
	 *         input to this output.
	 */
	public String toString() {
		String str = parent.getInput() + " -> " + this.getOutput();
		return str;
	}

	/**
	 * Generates a noise offset from the normal distribution with a mean of 0
	 * and a user-supplied standard deviation
	 * 
	 * @param stdDev
	 *            the (integer) standard deviation
	 * @return noise
	 */
	private double getNoise(int stdDev) {
		Random r = new Random();
		double result = 0;
		for (int i = 0; i < stdDev; i++)
			result += r.nextGaussian();
		return result;
	}

}

