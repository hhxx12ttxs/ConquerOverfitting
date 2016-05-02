package perceptron;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Random;

/**
 * The linguistic representations for HaLP. The centerpiece of the presentation.
 * <p>
 * A tableauSet is a list of OT tableaux that share a constraint ranking, as
 * shown in this schematic example:
 * </p>
 * 
 * <pre>
 *                 Max    Dep    *Coda
 * 
 * /tap/   
 *      1.0 [tap]                  *
 *      0.0 [ta]    *               
 * 
 * /am/   
 *      0.2 [?am]          *       *
 *      0.8 [?a]    *      *
 * </pre>
 * 
 * <p>
 * The example shows two tableaux that share the constraint hierarchy Max >> Dep
 * >> *Coda. Each tableau has an input and one or more outputs. Each output is
 * associated with a frequency, or its likelihood to win (1 vs. 0 is the same as
 * a hand vs. no hand, other relationships tell you what the distribution of
 * each output is in the data.
 * </p>
 * 
 * <p>
 * Each output is associated with a violation vector, or an ordered n-tuple of
 * violations.
 * </p>
 * 
 * <p>
 * The class assures that only one list of constraints is available for all the
 * tableaux that belong to it.
 * </p>
 * 
 * @author Michael Becker
 * @author Christopher Potts
 * @version 2.0 created 2007-02-10; last update 2007-02-22
 * 
 */

public class TableauSet {

	/*
	 * ================================================================= Class
	 * variables
	 * =================================================================
	 */

	/**
	 * Constraint family multipliers: If the user wants to have a certain family
	 * of constraints be adjusted faster or slower, they can specify how much
	 * the adjustments for each class should be multiplied by.
	 */

	private double markMultiplier = 2.0;
	private double ioFaithMultiplier = 2.0;
	private double ooFaithMultiplier = 1.0;

	/** The tableaux: An ArrayList of Tableau objects. */
	private ArrayList<Tableau> tableaux;

	/** The constraints: An ArrayList of LinguisticConstraint objects. */
	private ArrayList<LinguisticConstraint> con;

	/**
	 * The languages: An ArrayList of ArrayLists of Candidate objects. This
	 * member is used in the calculation of typologies, or sets of potential
	 * winners. This is different from the concept of "grammar", which is simply
	 * a constraint ranking. A language is a list of input/output mappings.
	 */
	private ArrayList<Language> languages;

	/**
	 * The name of the file that the TableauSet was read from
	 * 
	 */
	private String fileName = "";

	/**
	 * Status string, indicating whether the system has been solved, rendered
	 * infeasible, etc.
	 * 
	 * @author Christopher Potts
	 * @see edu.umass.linguist.JavaTableau.TableauSet#insertOptimalWeights()
	 * @see edu.umass.linguist.JavaTableau.TableauSet#solutionsToString()
	 */
	private int status = UNSOLVED;

	public static final int UNSOLVED = 0;
	public static final int FEASIBLE = 1;
	public static final int INFEASIBLE = 2;
	public static final int UNBOUNDED = 3;
	public static final int NONCONVERGENT = 4;

	// Added!
	public final static int PRINT_PLAIN_TEXT = 0;
	public final static int PRINT_HTML_OT = 1;
	public final static int PRINT_HTML_TYPOLOGY = 2;

	/**
	 * Minimal value for contraint weights, i.e., minimal values for the
	 * righthand sides of our equations on the LP side. This can optionally be
	 * given in row 1, tab-position 2, of the OTSoft file.
	 */
	public double minConstraintWeight = 0.0;

	private double rate = 0.0;

	private double outputStage = 0.0;

	private double noise = 0.2;

	private boolean lexicallySpecificConstraints;

	/*
	 * =================================================================
	 * Constructors
	 * =================================================================
	 */

	public void TableauSet() {
		// Making sure there are no null pointer errors
		ArrayList<LinguisticConstraint> blank = new ArrayList<LinguisticConstraint>();
		this.setConstraints(blank);
		ArrayList<Tableau> blank2 = new ArrayList<Tableau>();
		this.setTableaux(blank2);
	}

	/*
	 * ================================================================= Info
	 * about the system
	 * =================================================================
	 */

	/** Get the status string. */
	public int getStatus() {
		return this.status;
	}

	/**
	 * Set the status string.
	 * 
	 * @param str
	 *            The status of the system (solved, infeasible, unbounded, ...).
	 * @return true
	 */
	public boolean setStatus(int i) {
		this.status = i;
		return true;
	}

	/**
	 * Get the value for the RHS.
	 * 
	 * @return minRHS A double.
	 */
	public double getMinConstraintWeight() {
		return minConstraintWeight;
	}

	/**
	 * Set the minimal weight for each constraint. This can optionally be given
	 * in row 1, tab-position 2, of the OTSoft file.
	 * 
	 * @param newMin
	 * @return true
	 */
	public boolean setMinConstraintWeight(double newMin) {
		this.minConstraintWeight = newMin;
		return true;
	}

	public void resetWeights() {

		Iterator it = this.getConstraints().iterator();
		while (it.hasNext()) {
			LinguisticConstraint cons = (LinguisticConstraint) it.next();
			cons.setWeight(cons.getMinConstraintWeight());
		}

	}

	public double getRate() {
		return this.rate;
	}

	public boolean setRate(double dbl) {
		this.rate = dbl;
		return true;
	}

	public double getOutputStage() {
		return this.outputStage;
	}

	public boolean setOutputStage(double dbl) {
		this.outputStage = dbl;
		return true;
	}

	public double getNoise() {
		return this.noise;
	}

	public boolean setNoise(double dbl) {
		this.noise = dbl;
		return true;
	}

	/*
	 * ================================================================= The
	 * tableaux
	 * =================================================================
	 */

	/**
	 * Returns the total number of tableaux in this set.
	 * 
	 * @return this.tableaux.size() The number of tableaux.
	 */
	public int size() {
		return this.tableaux.size();
	}

	/**
	 * Returns an ArrayList of Tableau objects.
	 * 
	 * @return this.tableaux An ArrayList of Tableau objects.
	 */
	public ArrayList<Tableau> getTableaux() {
		return this.tableaux;
	}

	public int getNumberOfTableaux() {
		return this.tableaux.size();
	}
	
	public void setTableaux(ArrayList<Tableau> newTableaux) {
		this.tableaux = newTableaux;
	}

	/**
	 * Get a Tableau via its index.
	 * 
	 * @param i
	 *            The index of the tableau.
	 * @return the ith Tableau object.
	 */
	public Tableau getTableau(int i) {
		if (this.tableaux == null) {
			System.out.println("ERROR!");
		}
		return this.tableaux.get(i);
	}

	/**
	 * Add a tableau to this set.
	 * 
	 * @param tableau
	 *            The Tableau object to be added.
	 * @return addedSuccessfully A boolean variable.
	 */
	public boolean addTableau(Tableau tableau) {
		// boolean addedSuccessfully = true;
		// Make sure that the added tableau has the same constraint set as the
		// TableauSet.
		// if (tableau.getCon() == this.con) {
		if (this.tableaux == null) {
			this.tableaux = new ArrayList<Tableau>();
		}
		this.tableaux.add(tableau);
		// } else {
		// addedSuccessfully = false;
		// }
		return true;
	}

	/**
	 * Remove a tableau from this set.
	 * 
	 * @param i
	 *            The index of the Tableau to remove.
	 * @return The Tableau object that was removed.
	 */
	public Tableau removeTableau(int i) {
		return this.tableaux.remove(i);
	}

	/*
	 * ================================================================= The
	 * constraints.
	 * =================================================================
	 */

	/**
	 * Get the constraint Count for this tableau set.
	 * 
	 * @return int The number of constraints.
	 */
	public int constraintCount() {
		return this.con.size();
	}

	/**
	 * Get the constraints.
	 * 
	 * @return this.con The constraints.
	 */
	public ArrayList<LinguisticConstraint> getConstraints() {
		return this.con;
	}

	/**
	 * Get the constraint at the specified index.
	 * 
	 * @param i
	 *            The index.
	 * @return The constraint at index i.
	 */
	public LinguisticConstraint getConstraint(int i) {
		return this.con.get(i);
	}

	/**
	 * Add a constraint.
	 * 
	 * @param constraint
	 *            The LinguisticConstraint to add.
	 * @return true if the constraint is added, else false.
	 */
	public boolean addConstraint(LinguisticConstraint constraint) {
		if (this.con == null)
			this.con = new ArrayList<LinguisticConstraint>();
		return this.con.add(constraint);
	}

	public boolean setConstraints(ArrayList<LinguisticConstraint> constraints) {
		this.con = constraints;
		return true;
	}

	/*
	 * ================================================================= Errors
	 * and warnings.
	 * =================================================================
	 */

	/** An ArrayList of error messages. */
	private ArrayList<String> errorMessages = new ArrayList<String>();

	/** An ArrayList of warning messages. */
	private ArrayList<String> warnings = new ArrayList<String>();

	/**
	 * Gets the error messages.
	 * 
	 * @return this.errorMessages An ArrayList of errors.
	 */
	public ArrayList<String> getErrors() {
		return this.errorMessages;
	}

	/**
	 * Add an error message.
	 * 
	 * @param error
	 *            A string describing the error.
	 * @return true
	 */
	public boolean addError(String error) {
		this.errorMessages.add(error);
		return true;
	}

	/**
	 * Gets the warning messages.
	 * 
	 * @return this.warnings An ArrayList of warnings.
	 */
	public ArrayList<String> getWarnings() {
		return this.warnings;
	}

	/**
	 * Add a warning message.
	 * 
	 * @param warning
	 *            A warning message (string).
	 * @return true
	 */
	public boolean addWarning(String warning) {
		this.warnings.add(warning);
		return true;
	}

	/*
	 * =================================================================
	 * String-to-number conversions.
	 * =================================================================
	 */

	/**
	 * Parse the input string as a double object.
	 * 
	 * @param str
	 *            The string to turn into a double.
	 * @return result The double object.
	 * @throws NumberFormatException
	 *             returns 0.0 if this exception occurs.
	 */
	private double readDouble(String str) {
		double result = 0.0;
		try {
			result = Double.parseDouble(str);
		} catch (NumberFormatException exc) {
			return 0.0;
		}
		return result;
	}

	/**
	 * Parse the input string as an integer.
	 * 
	 * @param str
	 *            The string to parse.
	 * @return result The integer object.
	 * @throws NumberFormatException
	 *             returns 0 if this exception occurs.
	 */
	/*
	 * private int readInteger(String str) { int result = 0; try { result =
	 * Integer.parseInt(str); } catch (NumberFormatException exc) { return 0; }
	 * return result; }
	 */

	/*
	 * =================================================================
	 * Printing.
	 * =================================================================
	 */

	/**
	 * Print out the constraints, tab separated.
	 * 
	 * @return str The string representing the constraints.
	 */
	public String constraintsToString() {
		String str = "";
		for (int i = 0; i < constraintCount(); i++) {
			LinguisticConstraint thisCon = con.get(i);
			String shortname = thisCon.getShortName();
			str += shortname + "_";
		}
		return str;
	}

	/**
	 * Prints the current tableau set.
	 * 
	 * @param out
	 *            The PrintStream --- where the content will end up.
	 */
	public void print(PrintStream out, String type) {

		// Stylesheet.
		String PO = "<p>";
		String PC = "</p>";
		String STATUSO = "<h3>";
		String STATUSC = "</h3>";
		if (type == "plain") {
			PO = PC = STATUSO = STATUSC = "";
		}

		// Print the system status.
		String str = "LP status: ";
		if (this.status == TableauSet.FEASIBLE)
			str += "solved";
		if (this.status == TableauSet.INFEASIBLE)
			str += "infeasible";
		if (this.status == TableauSet.NONCONVERGENT)
			str += "non-convergent";
		if (this.status == TableauSet.UNBOUNDED)
			str += "unbounded";
		if (this.status == TableauSet.UNSOLVED)
			str += "unsolved";
		out.println(STATUSO + str + STATUSC);

		// Print the system status.
		out.println(STATUSO + "Min constraint weight was set to "
				+ this.minConstraintWeight + STATUSC);

		// Print any error messages.
		printErrors(out, type);

		// Plain-text output.
		if (type == "plain") {
			// Print the system in plain text.
			printPlainText(out);
			// Print warnings if there are any.
			if (this.getWarnings().size() != 0) {
				out.println("Non-deal-breaking issue(s):");
				Iterator warn = this.getWarnings().iterator();
				while (warn.hasNext()) {
					out.println(warn.next());
				}
			}
			// HTML output.
		} else {
			// Print warnings if there are any.
			if (this.getWarnings().size() != 0) {
				out.println(STATUSO
						+ "HaLP encountered some issue(s) while running."
						+ STATUSC);
				out
						.println(PO
								+ "<a href='javascript:void(0)' onclick='hideReveal(\"issues\").style.display = \"block\";'>Show/Hide issue(s)</a>"
								+ PC);
				out.println("<div id='issues' style='display: none;'>");
				out.println("<ul>");
				Iterator warn = this.getWarnings().iterator();
				while (warn.hasNext()) {
					out.println("<li>" + warn.next() + "</li>");
				}
				out.println("</ul>");
				out.println("</div>");
			}
			// Print the system in HTML.
			this.printHTML(type, out);
		}
	}

	/**
	 * Print any error messages.
	 * 
	 * @param out
	 *            The output PrintStream.
	 * @param type
	 *            String indicating the type: plain or html.
	 */
	public void printErrors(PrintStream out, String type) {

		// Stylesheet.
		String PO = "<p>";
		String PC = "</p>";
		if (type == "plain") {
			PO = PC = "";
		}

		// If there are errors, print them and stop.
		if (this.getErrors().size() != 0) {
			// / Print errors.
			out.println(PO + "Error message(s):" + PC);
			Iterator err = this.getErrors().iterator();
			while (err.hasNext()) {
				out.println(PO + err.next() + PC);
			}
		}
	}

	/**
	 * Prints the current tableaux in HTML format.
	 * 
	 * @param type
	 * @param <PrintStream>
	 *            out The place the content ends up.
	 */
	private void printHTML(String type, PrintStream out) {

		// HTML mark-up and styles.
		String TABLEO = "<table class='tableau' border='1'>";
		String TABLEC = "</table>\n\n";
		String TRO = "<tr>";
		String TRC = "</tr>";
		String TD = "<td></td>";
		String TDOcenter = "<td align='center'>";
		String TDOleft = "<td align='left'>";
		String TDOright = "<td align='right'>";
		String TDOfixed = "<td class='output'>";
		String TDC = "</td>";
		String WINNER_MARK = "<img src='images/hand.png' width='30' height='15' alt='===&gt;' />&nbsp;&nbsp;";

		if (type == "html_typology") {
			WINNER_MARK = "<B>--></B> &nbsp; ";
		}

		String line = "";

		// ////////////////////////////////////////////////////////////////////
		// Print a tableau with just the constraints and their weights.
		line = TABLEO;
		out.println(line);
		// Get and print the constraints, as a table row.
		ArrayList<LinguisticConstraint> con = this.getConstraints();
		Iterator conIt = con.iterator();
		line = TRO;
		if (conIt.hasNext()) {
			line += TD;
		}
		while (conIt.hasNext()) {
			LinguisticConstraint currCon = (LinguisticConstraint) conIt.next();
			line += TDOcenter + currCon.getShortName() + TDC;
		}
		line += TRC;
		out.println(line);
		// Get and print the weights, as a table row.
		line = TRO;
		conIt = con.iterator();
		if (conIt.hasNext()) {
			line += TDOright + "<em>Weights</em>" + TDC;
		}
		while (conIt.hasNext()) {
			LinguisticConstraint currCon = (LinguisticConstraint) conIt.next();
			line += TDOcenter + currCon.getRoundedWeight() + TDC;
		}
		line += TRC;
		out.println(line);
		line = TABLEC;
		out.println(line);
		// ////////////////////////////////////////////////////////////////////

		// / Print the tableaux.
		ArrayList<Tableau> tableaux = this.getTableaux();
		Iterator tabIt = tableaux.iterator();
		int candCount = 0;
		while (tabIt.hasNext()) {
			line = "";
			Tableau tableau = (Tableau) tabIt.next();
			line += TABLEO;
			out.println(line);

			// Get and print the weights, as a table row.
			line = TRO;
			conIt = con.iterator();
			if (conIt.hasNext()) {
				line += TDOright + "<em>Weights</em>" + TDC;
			}
			while (conIt.hasNext()) {
				LinguisticConstraint currCon = (LinguisticConstraint) conIt
						.next();
				line += TDOcenter + currCon.getRoundedWeight() + TDC;
			}
			line += TRC;
			out.println(line);

			line = TRO;
			// / Print input.
			line += TDOfixed + "<em>Input</em>:&nbsp;&nbsp;"
					+ tableau.getInput() + TDC;
			// Get and print the constraints, as a table row.
			conIt = con.iterator();
			while (conIt.hasNext()) {
				LinguisticConstraint currCon = (LinguisticConstraint) conIt
						.next();
				line += TDOcenter + currCon.getShortName() + TDC;
			}
			line += TRC;
			out.println(line);
			line = "";

			// / Print candidates.
			ArrayList<Candidate> candidates = tableau.getCandidates();
			Iterator cand_it = candidates.iterator();
			while (cand_it.hasNext()) {
				line = TRO;
				Candidate cand = (Candidate) cand_it.next();
				candCount++;
				// / Print output.
				if (cand.getFrequency() == 0) {
					line += TDOright;

					// / for typology: add a "make winner" link
					if (type == "html_typology") {
						line += "<A STYLE='color:333399;' href='HaLP:manualTypology("
								+ candCount
								+ ")'>"
								+ cand.getOutput()
								+ "</a>"
								+ TDC;
					} else {
						line += cand.getOutput() + TDC;
					}

				} else {
					line += TDOright;
					line += WINNER_MARK + cand.getOutput() + TDC;
				}
				// / Print violations.
				ArrayList<Cell> cells = cand.getViolationVector();
				Iterator cell_it = cells.iterator();
				while (cell_it.hasNext()) {
					Cell cell = (Cell) cell_it.next();
					line += TDOcenter + cell.getViolation() + TDC;
				}
				// / Print weighted total.
				line += TDOleft + "Weighted total: " + cand.getRoundedWeight()
						+ TDC;
				line += TRC;
				out.println(line);
				line = "";
			}
			line = TABLEC;
			out.println(line);
		}
	}

	/**
	 * Prints an OTSoft file in plain-text, with the weights included. It is
	 * just a call to printPlainly with the second parameter set to
	 * <code>plain</code>.
	 * 
	 * @param out
	 *            The output stream.
	 */
	private void printPlainText(PrintStream out) {
		printPlainly(out, "plain");
	}

	/**
	 * Prints an OTSoft file in plain-text, no weights. It is just a call to
	 * printPlainly with the second parameter set to <code>otsoft</code>.
	 * 
	 * @param out
	 *            The output stream.
	 */
	@SuppressWarnings("unused")
	private void printOTSoftFile(PrintStream out) {
		printPlainly(out, "otsoft");
	}

	/**
	 * Prints the current tableaux in plain-text.
	 * 
	 * @param <PrintStream>
	 *            out The place the content ends up.
	 * @param <String>
	 *            type If this is <code>plain</code> then the weights are
	 *            printed. Anything else, and the weights are not printed.
	 */
	private void printPlainly(PrintStream out, String type) {
		String line = "";
		// Print the system status (added by chris).
		line += "System status: ";
		if (this.status == TableauSet.FEASIBLE)
			line += "solved";
		if (this.status == TableauSet.INFEASIBLE)
			line += "infeasible";
		if (this.status == TableauSet.NONCONVERGENT)
			line += "non-convergent";
		if (this.status == TableauSet.UNBOUNDED)
			line += "unbounded";
		if (this.status == TableauSet.UNSOLVED)
			line += "unsolved";

		out.println(line);
		line = "";
		// / Print constraints' full names.
		ArrayList<LinguisticConstraint> con = this.getConstraints();
		Iterator it = con.iterator();
		if (it.hasNext()) {
			line += "\t\t";
		}
		while (it.hasNext()) {
			LinguisticConstraint currCon = (LinguisticConstraint) it.next();
			line += "\t" + currCon.getName();
		}
		out.println(line);
		// / Print constraints' short names.
		line = "";
		it = con.iterator();
		if (it.hasNext()) {
			line += "\t\t";
		}
		while (it.hasNext()) {
			LinguisticConstraint currCon = (LinguisticConstraint) it.next();
			line += "\t" + currCon.getShortName();
		}
		out.println(line);
		line = "";
		// / Print constraint weights.
		if (type == "plain") {
			it = con.iterator();
			if (it.hasNext()) {
				line += "weights:\t\t";
			}
			while (it.hasNext()) {
				LinguisticConstraint currCon = (LinguisticConstraint) it.next();
				line += "\t" + currCon.getWeight();
			}
			out.println(line);
		}
		// / Print the tableaux.
		ArrayList<Tableau> tableaux = this.getTableaux();
		it = tableaux.iterator();
		while (it.hasNext()) {
			line = "";
			Tableau tableau = (Tableau) it.next();
			// / Print input.
			line += tableau.getInput();
			// / Print candidates.
			ArrayList<Candidate> candidates = tableau.getCandidates();
			Iterator cand_it = candidates.iterator();
			while (cand_it.hasNext()) {
				Candidate cand = (Candidate) cand_it.next();
				// / Print output.
				line += "\t" + cand.getOutput();
				// / Print output's frequency.
				if (cand.getFrequency() == 0) {
					line += "\t";
				} else {
					line += "\t" + cand.getFrequency();
				}
				// / Print violations.
				ArrayList<Cell> cells = cand.getViolationVector();
				Iterator cell_it = cells.iterator();
				while (cell_it.hasNext()) {
					Cell cell = (Cell) cell_it.next();
					line += "\t" + cell.getViolation();
				}
				// / Print weighted total.
				line = line + "\tWeighted total:\t" + cand.getWeight();
				out.println(line);
				line = "";
			}
		}
	}

	/**
	 * On frequencies: 0 = loser, anything bigger = potential winner. Candidate
	 * (array of strings):
	 * <code>line-number[0] input[1] output[2] frequency[3] violations[4..]</code>
	 * 
	 * @param <String>
	 *            currLine The line we are reading.
	 * @param <Tableau>
	 *            tableau The tableau to process.
	 * @return candidate The Candidate object processed.
	 */
	/*
	private Candidate readOTSoftCandidate(String[] currLine, Tableau tableau) {
		Candidate candidate = new Candidate(tableau);
		LinguisticForm output = new LinguisticForm(currLine[2].trim());
		candidate.setOutput(output);
		// Check the forms to make sure they have the requisite parts.
		// System.out.println(candidate.toString());
		// / Read frequency.
		if (currLine.length > 3) {
			if (currLine[3].trim().length() > 0) {
				candidate.setFrequency(readDouble(currLine[3].trim()));
			}
		} else {
			// / No frequency found - add zero.
			candidate.setFrequency(0);
		}
		// / Read violation marks, if any.
		for (int i = 4; i < currLine.length; i++) {
			// / Read existing violation marks.
			Cell cell = new Cell(this.readDouble(currLine[i].trim()));
			candidate.addViolation(cell);
		}
		try {
			if (candidate.getViolationVector().size() < this.con.size()) {
				// String line_num = currLine[0];
				// This warnings seemed like overkill. --- Chris (2007-02-21)
				// this.addWarning("The number of violation marks in line no. "
				// + line_num +
				// " of your OTSoft file did not match the number of constraints; zeroes were added as necessary.");
				for (int i = candidate.getViolationVector().size(); i < this.con
						.size(); i++) {
					// / Add zeroes if no enough violation marks were found in
					// file;
					// / make the violation vector as long as CON.
					Cell cell = new Cell(0);
					candidate.addViolation(cell);
				}
			}
		} catch (NullPointerException e) {
			String line_num = currLine[0];
			this
					.addError("There was a problem reading the violation marks in line no. "
							+ line_num
							+ " of your OTSoft file. Please add 0s to one or more of the empty cells in that line.");
			return candidate;
		}
		return candidate;
	}

	/**
	 * Read in an OTSoft file.
	 * 
	 * @param OTsoftfile
	 *            An ArrayList of String object.
	 * @return true if the file was read correctly, else false.
	 */
	/*
	public boolean readOTSoftFile(ArrayList<String> OTsoftfile) {
		boolean readSuccessfully = true;
		Iterator<String> file = OTsoftfile.iterator();

		// / Get first two lines, hope to find constraints in them.
		// Constraints are counted via their tab spaces.
		Pattern pat1 = Pattern.compile("\\t");
		String[] con1 = pat1.split(file.next());
		String[] con2 = pat1.split(file.next());
		// Get the next line, looking for constraint groups.
		String[] groups = pat1.split(file.next());

		// If the constraint counts in the first two lines don't match, quit.
		if (con1.length != con2.length) {
			String line_num1 = con1[0], line_num2 = con2[0];
			this
					.addError("The number of constraints found in line no. "
							+ line_num1
							+ " of your OTSoft file did not match the number of constraints found in line no. "
							+ line_num2 + " of that file.");
			readSuccessfully = false;
		} else {
			if (con1.length < 5) { // tab-position 5 is the first place a
									// constraint can sit.
				String line_num1 = con1[0];
				this.addError("No constraint names were found in line no. "
						+ line_num1 + " of your OTSoft file");
				readSuccessfully = false;
			} else {
				// Look for a default value.
				String flt = "(\\+|-)?\\d+(\\.\\d+)?";
				Pattern weightPattern = Pattern.compile(flt);
				Matcher weightMatcher = weightPattern.matcher(con1[2]);
				if (weightMatcher.find()) {
					double minCW = readDouble(weightMatcher.group());
					this.setMinConstraintWeight(minCW);
				}
				// / Read constraint names and add them.
				for (int i = 4; i < con1.length; i++) {
					LinguisticConstraint cons = new LinguisticConstraint();
					cons.setName(con1[i]);
					cons.setShortName(con2[i]);
					cons.setHTMLName(con2[i]);
					if (groups[i].toLowerCase().equals("m"))
						cons.setMarkedness();
					if (groups[i].toLowerCase().equals("i"))
						cons.setIOFaithfulness();
					if (groups[i].toLowerCase().equals("o"))
						cons.setOOFaithfulness();
					this.addConstraint(cons);
					// FIXME
					System.out.println(cons.getShortName());
				}
				// // Read the rest of the file, looking for tableaux.
				while (file.hasNext()) {
					String[] currLine = pat1.split(file.next());
					if (currLine.length < 3) { // tab-position 3 is the first
												// place an output can sit
						String line_num = currLine[0];
						this.addError("Line no. " + line_num
								+ " of you OTSoft file could not be read.");
						readSuccessfully = false;
					} else {
						if (currLine[1].trim().length() > 0) {
							// / Found input, make new tableau.
							Tableau tableau = new Tableau(this);
							LinguisticForm input = new LinguisticForm(currLine[1]);
							tableau.setInput(input);
							tableau.addCandidate(readOTSoftCandidate(currLine,tableau));
							System.out.println("DEBUG: Adding Tableau!");
							this.addTableau(tableau);
						} else {
							// / Input not found, try to append to current
							// tableau,
							// / or report error if there isn't one.
							if (this.getTableaux().size() == 0) {
								String line_num = currLine[0];
								this.addError("Line no. " + line_num + " in your OTSoft file could not be associated with any input.");
							} else {
								// / Add candidate to the last tableau.
								Tableau tableau = this.getTableaux().get(this.getTableaux().size() - 1);
								// FIXME
								System.out.println(readOTSoftCandidate(currLine, tableau).getName());
								tableau.addCandidate(readOTSoftCandidate(currLine, tableau));
								
							}
						}
					}
				}
				if (this.getTableaux().size() == 0) {
					this.addWarning("No tableaux were found in your OTSoft file.");
				}
			}
		}
		// Check that no tableau has more than one winner.
		for (int i = 0; i < this.size(); i++) {
			if (this.getTableau(i).winnerCount() > 1) {
				int count = i + 1;
				this.addError("Tableau " + count + " has more than one winner.");
			}
		}

		return readSuccessfully;
	}
	
	/**
	 * Get the total number of candidates in a TableauSet
	 * 
	 * @return int the number of candidates in the TableauSet
	 */
	
	private Candidate readOTSoftCandidate(String[] currLine, Tableau tableau) {	
		Candidate candidate = new Candidate(tableau);
		LinguisticForm output = new LinguisticForm(currLine[2].trim());
		candidate.setOutput(output);
		// Check the forms to make sure they have the requisite parts.
		// System.out.println(candidate.toString());
		/// Read frequency.
		if (currLine.length > 3) {
			if (currLine[3].trim().length() > 0) {
				candidate.setFrequency(readDouble(currLine[3].trim()));
			}
		} else {
			/// No frequency found - add zero.
			candidate.setFrequency(0);
		}
		/// Read violation marks, if any.
		for (int i = 4; i < currLine.length; i++) {
			/// Read existing violation marks.
			Cell cell = new Cell(this.readDouble(currLine[i].trim()));
			candidate.addViolation(cell);
		}

		/// add zeros as necessary to make the violation vector as big as the constraint set
		for (int i = candidate.getViolationVector().size() ; i < this.con.size() ; i++) {
			Cell cell = new Cell(0);
			candidate.addViolation(cell);
		}
		
		/* try {
			if (candidate.getViolationVector().size() < this.con.size()) {
				//String line_num = currLine[0];
				// This warnings seemed like overkill. --- Chris (2007-02-21)
				//this.addWarning("The number of violation marks in line no. " + line_num + " of your OTSoft file did not match the number of constraints; zeroes were added as necessary.");
				for (int i = candidate.getViolationVector().size() ; i < this.con.size() ; i++) {
					/// Add zeroes if no enough violation marks were found in file;
					/// make the violation vector as long as CON.
					Cell cell = new Cell(0);
					candidate.addViolation(cell);
				}
			}
		} catch (NullPointerException e) {
			String line_num = currLine[0];
			this.addError("There was a problem reading the violation marks in line no. " + line_num + " of your OTSoft file. Please add 0s to one or more of the empty cells in that line.");
			return candidate; 
		}  */

		
		
		
		return candidate;
	}
	
	public void setLexicallySpecificConstraints(boolean b) {
		this.lexicallySpecificConstraints = b;
	}
	
	/** Read in an OTSoft file.
	 * @param OTsoftfile An ArrayList of String object.
	 * @return true if the file was read correctly, else false.
	 */
	public boolean readOTSoftFile (ArrayList<String> OTsoftfile) {
		boolean readSuccessfully = true;
		Iterator<String> file = OTsoftfile.iterator();
		
		/// Get first two lines, hope to find constraints in them.
		//  Constraints are counted via their tab spaces.
		Pattern pat1 = Pattern.compile("\\t");
		String[] con1 = pat1.split(file.next());
		String[] con2 = pat1.split(file.next());

		// If the constraint counts in the first two lines don't match, quit.
		if (con1.length != con2.length) {
			String line_num1 = con1[0], line_num2 = con2[0]; 
			this.addError("The number of constraints found in line no. " + line_num1 + " of your OTSoft file did not match the number of constraints found in line no. " + line_num2 + " of that file.");
			readSuccessfully = false;
		} else {
			if (con1.length < 5) { // tab-position 5 is the first place a constraint can sit.
				String line_num1 = con1[0]; 
				this.addError("No constraint names were found in line no. " + line_num1 + " of your OTSoft file");
				readSuccessfully = false;
			} else {
				// Look for a default value.
				String flt = "(\\+|-)?\\d+(\\.\\d+)?";
				Pattern weightPattern = Pattern.compile(flt);
				Matcher weightMatcher = weightPattern.matcher(con1[2]);
				if ( weightMatcher.find() ) {
					double minCW = readDouble(weightMatcher.group());
					this.setMinConstraintWeight(minCW);
				}
				/// Read constraint names and add them.
				for (int i = 4; i < con1.length; i++) {
					LinguisticConstraint cons = new LinguisticConstraint(this);
					cons.setName(con1[i]);
					cons.setShortName(con2[i]);
					cons.setHTMLName(con2[i]);
					this.addConstraint(cons);
				}				
				//// Read the rest of the file, looking for tableaux.
				
				boolean reachedEndOfTableaux = false;
				
				while (file.hasNext()) {
					String[] currLine  = pat1.split(file.next());
					
					if (currLine[1].equalsIgnoreCase("[end of tableaux]")) reachedEndOfTableaux = true;
					
					if (!reachedEndOfTableaux) {
						
						/// read tableaux
						
						if (currLine.length < 3) { // tab-position 3 is the first place an output can sit
							String line_num = currLine[0];
							this.addError("Line no. " + line_num + " of you OTSoft file could not be read.");
							readSuccessfully = false;
						} else {
							if (currLine[1].trim().length() > 0) {
								/// Found input, make new tableau.
								Tableau tableau = new Tableau(this);							 
								LinguisticForm input = new LinguisticForm(currLine[1]);
								tableau.setInput(input);							
								tableau.addCandidate(readOTSoftCandidate(currLine,tableau));							
								this.addTableau(tableau);							
							} else {
								/// Input not found, try to append to current tableau,
								/// or report error if there isn't one.
								if (this.getTableaux().size() == 0) {
									String line_num = currLine[0];
									this.addError("Line no. " + line_num + " in your OTSoft file could not be associated with any input.");
								} else {
									/// Add candidate to the last tableau.
									Tableau tableau = this.getTableaux().get(this.getTableaux().size() - 1);
									tableau.addCandidate(readOTSoftCandidate(currLine,tableau));
								}
							}
						}
					} else {
						//// read advanced options
						
						/// lexically specific constraint - read true or false
						if (currLine[1].trim().equalsIgnoreCase("[lexically specific constraints]")) {
							if (currLine[2].trim().length()>0) {
								if (currLine[2].trim().equals("0")) {
									this.setLexicallySpecificConstraints(false);
								} else {
									this.setLexicallySpecificConstraints(true);
								}
							}
						}
						
						///rate - read double
						if (currLine[1].trim().equalsIgnoreCase("[rate]")) {
							if (currLine.length>2 && currLine[2].trim().length()>0) {
								this.setRate(readDouble(currLine[2].trim()));
							}
						}
						
						/// output stage - read double
						if (currLine[1].trim().equalsIgnoreCase("[output stage]")) {
							if (currLine.length>2 && currLine[2].trim().length()>0) {
								this.setOutputStage(readDouble(currLine[2].trim()));
							}
						}
						
						/// biases - read one double per constraint
						if (currLine[1].trim().equalsIgnoreCase("[bias]")) {
							for (int i = 4 ; i < currLine.length ; i++) {
								if (currLine[i].trim().length()>0) {
									double bias = readDouble(currLine[i].trim());
									if ((i-4) < this.getConstraints().size()) {
										this.getConstraint(i-4).setBias(bias);
									}
								}
							}
						}
						
						/// initial rankings - read one double per constraint
						if (currLine[1].trim().equalsIgnoreCase("[initial ranking]")) {
							for (int i = 4 ; i < currLine.length ; i++) {
								if (currLine[i].trim().length()>0) {
									double initialRanking = readDouble(currLine[i].trim());
									if ((i-4) < this.getConstraints().size()) {
										this.getConstraint(i-4).setInitialRanking(initialRanking);
									}
								}
							}
						}

						/// read minimal weights
						/// read column 2 as default double value, then read one double per constraint, which overrides the column 2 value
						if (currLine[1].trim().equalsIgnoreCase("[minimal weight]")) {
							if (currLine.length>2 && currLine[2].trim().length()>0) {
								this.setMinConstraintWeight(readDouble(currLine[2].trim()));
							}
							for (int i = 4 ; i < currLine.length ; i++) {
								if (currLine[i].trim().length()>0) {
									double minWeight = readDouble(currLine[i].trim());
									if ((i-4) < this.getConstraints().size()) {
										this.getConstraint(i-4).setUseLocalMinConstraintWeight(true);
										this.getConstraint(i-4).setMinConstraintWeight(minWeight);
									}
								}
							}
						}
						
						/// read noise
						/// read column 2 as default double value, then read one double per constraint, which overrides the column 2 value
						if (currLine[1].trim().equalsIgnoreCase("[noise]")) {
							if (currLine.length>2 && currLine[2].trim().length()>0) {
								this.setNoise(readDouble(currLine[2].trim()));
							}
							for (int i = 4 ; i < currLine.length ; i++) {
								if (currLine[i].trim().length()>0) {
									double noise = readDouble(currLine[i].trim());
									if ((i-4) < this.getConstraints().size()) {
										this.getConstraint(i-4).setUseLocalNoise(true);
										this.getConstraint(i-4).setNoise(noise);
									}
								}
							}
						}	
					}
				}
				if (this.getTableaux().size() == 0) {
					this.addWarning("No tableaux were found in your OTSoft file.");
				}
			}
		}
		// Check that no tableau has more than one winner.
		for (int i = 0; i < this.size(); i++) {
			if (this.getTableau(i).winnerCount() > 1) {
				int count = i+1;
				this.addError("Tableau " + count + " has more than one winner.");
			}
		}
				
		return readSuccessfully;	
	}

	
	
	
	public int getCandidateCount() {

		
		
		Iterator it = this.getTableaux().iterator();
		
		
		int totalCandidates = 0;
		
		while (it.hasNext()) {
			Tableau tableau = (Tableau) it.next();
			Iterator it2 = tableau.getCandidates().iterator();
			while (it2.hasNext()) {
				totalCandidates++;
				it2.next();
			}
		}
		return totalCandidates;

	}

	/**
	 * Set the adjustment multiplier for marknedness constraints
	 * 
	 * @param x
	 *            the new multiplier
	 */
	public void setMarkMultiplier(double x) {
		this.markMultiplier = x;
	}

	/**
	 * Set the adjustment multiplier for IO-Faithfulness constraints
	 * 
	 * @param x
	 *            the new multiplier
	 */
	public void setIOFaithMultiplier(double x) {
		this.ioFaithMultiplier = x;
	}

	/**
	 * Set the adjustment multiplier for OO-Faithfulness constraints
	 * 
	 * @param x
	 *            the new multiplier
	 */
	public void setOOFaithMultiplier(double x) {
		this.ooFaithMultiplier = x;
	}

	public double getMarkMultiplier() {
		return markMultiplier;
	}

	public double getIOFaithMultiplier() {
		return ioFaithMultiplier;
	}

	public double getOOFaithMultiplier() {
		return ooFaithMultiplier;
	}

	/**
	 * Get the number of possible langugages in a TableauSet See member
	 * "languages" for an explanation of what a language is in this context
	 * 
	 * @return int the number of languages in the TableauSet
	 */
	public int getLanguageCount() {

		Iterator it = this.getTableaux().iterator();
		int totalLanguages = 1;

		while (it.hasNext()) {
			Tableau tableau = (Tableau) it.next();
			int currentTableauCandidates = 0;
			Iterator it2 = tableau.getCandidates().iterator();
			while (it2.hasNext()) {
				currentTableauCandidates++;
				it2.next();
			}
			totalLanguages *= currentTableauCandidates;
		}
		return totalLanguages;

	}

	public ArrayList<Language> getLanguages() {

		if (this.languages == null)
			this.languages = new ArrayList<Language>();
		return this.languages;
	}

	public boolean setLanguages(ArrayList<Language> langs) {

		this.languages = langs;
		return true;
	}

	/*
	 * public void generateLanguages() {
	 * 
	 * ArrayList<Language> seedLanguages = new ArrayList<Language>();
	 * seedLanguages.add(new Language(null));
	 * 
	 * Iterator tableaux = this.getTableaux().iterator(); while
	 * (tableaux.hasNext()) { Tableau tabl = (Tableau) tableaux.next();
	 * ArrayList<Language> newLanguages = new ArrayList<Language>(); Iterator
	 * candidates = tabl.getCandidates().iterator(); while
	 * (candidates.hasNext()) { Candidate cand = (Candidate) candidates.next();
	 * Iterator it = seedLanguages.iterator(); while (it.hasNext()) { Language
	 * lang = (Language) it.next(); Language newLanguage = (Language)
	 * lang.clone(); newLanguage.add(cand); newLanguages.add(newLanguage); } }
	 * seedLanguages.clear(); Iterator it2 = newLanguages.iterator(); while
	 * (it2.hasNext()) { Language lang = (Language) it2.next(); Language
	 * newLanguage = (Language) lang.clone(); seedLanguages.add(newLanguage); }
	 * } this.languages = seedLanguages; }
	 */

	public ArrayList<Language> getFeasibleLanguages() {
		ArrayList<Language> feasibleLanguages = new ArrayList<Language>();
		if (this.languages != null) {
			Iterator it = this.languages.iterator();
			while (it.hasNext()) {
				Language lang = (Language) it.next();
				if (lang.getLPStatus() == TableauSet.FEASIBLE)
					feasibleLanguages.add(lang);
			}
		}
		return feasibleLanguages;
	}

	public ArrayList<Language> getRankableLanguages() {
		ArrayList<Language> rankableLanguages = new ArrayList<Language>();
		if (this.languages != null) {
			Iterator it = this.languages.iterator();
			while (it.hasNext()) {
				Language lang = (Language) it.next();
				if (lang.getRCDStatus() == ComparativeTableau.RANKING_FOUND)
					rankableLanguages.add(lang);
			}
		}
		return rankableLanguages;
	}

	public void setFrequenciesToZero() {

		Iterator tableaux = this.getTableaux().iterator();
		while (tableaux.hasNext()) {
			Tableau tabl = (Tableau) tableaux.next();
			Iterator candidates = tabl.getCandidates().iterator();
			while (candidates.hasNext()) {
				Candidate cand = (Candidate) candidates.next();
				cand.setFrequency(0);
			}
		}

	}

	public boolean setWinners(Language language) {
		Boolean success = true;

		// / verify that all the candidates belong to the current TableauSet
		Iterator it = language.getCandidates().iterator();
		while (it.hasNext()) {
			Candidate cand = (Candidate) it.next();
			if (!cand.getParent().getParent().equals(this)) {
				success = false;
			}
		}
		if (success) {
			this.setFrequenciesToZero();
			it = language.getCandidates().iterator();
			while (it.hasNext()) {
				Candidate cand = (Candidate) it.next();
				cand.setFrequency(1);
			}
		}
		return success;
	}

	public String getFileName() {
		return this.fileName;
	}

	public Boolean setFileName(String str) {
		this.fileName = str;
		return true;
	}

	public Candidate getCandidateByNumber(int winnerNum) {

		if ((winnerNum > 0) && (winnerNum <= this.getCandidateCount())) {

			Iterator it = this.getTableaux().iterator();
			int totalCandidates = 0;

			while (it.hasNext()) {
				Tableau tableau = (Tableau) it.next();
				Iterator it2 = tableau.getCandidates().iterator();
				while (it2.hasNext()) {
					totalCandidates++;
					Candidate cand = (Candidate) it2.next();
					if (totalCandidates == winnerNum) {
						return cand;
					}
				}
			}
		}
		return null;
	}

	public int getCandidateNumber(Candidate cand) {

		if (cand.getParent().getParent() == this) {
			Iterator it = this.getTableaux().iterator();
			int totalCandidates = 0;
			while (it.hasNext()) {
				Tableau tableau = (Tableau) it.next();
				Iterator it2 = tableau.getCandidates().iterator();
				while (it2.hasNext()) {
					totalCandidates++;
					Candidate currentCand = (Candidate) it2.next();
					if (currentCand == cand) {
						return totalCandidates;
					}
				}
			}
		}
		return 0;
	}

	public boolean makeWinner(Candidate cand) {

		boolean success = false;
		if (cand.getParent().getParent().equals(this)) {
			Tableau tabl = cand.getParent();
			Iterator it = tabl.getCandidates().iterator();
			while (it.hasNext()) {
				Candidate currCand = (Candidate) it.next();
				currCand.setFrequency(0);
			}
			cand.setFrequency(1);
			success = true;
		}
		return success;
	}

	public boolean hasUniqueWinners() {
		// / make sure that all tableaux have unique winners
		boolean allUniqueWinners = true;
		Iterator it = this.getTableaux().iterator();
		while (it.hasNext()) {
			Tableau tabl = (Tableau) it.next();
			if (!tabl.hasUniqueWinner())
				allUniqueWinners = false;
		}
		return allUniqueWinners;
	}

	public Hashtable<Candidate, Double> getCandidateFrequencies() {

		Hashtable<Candidate, Double> hash = new Hashtable<Candidate, Double>();

		Iterator it = this.getTableaux().iterator();
		while (it.hasNext()) {
			Tableau tableau = (Tableau) it.next();
			Iterator it2 = tableau.getCandidates().iterator();
			while (it2.hasNext()) {
				Candidate cand = (Candidate) it2.next();
				hash.put(cand, cand.getFrequency());
			}
		}
		return hash;
	}

	public void setCandidateFrequencies(Hashtable<Candidate, Double> hash) {

		Iterator it = (Iterator) hash.keys();
		while (it.hasNext()) {
			Candidate cand = (Candidate) it.next();
			if (cand.getParent().getParent() == this) {
				cand.setFrequency(hash.get(cand));
			}
		}
	}

	/**
	 * Takes a text file, removes blank line and comments lines, adds line
	 * numbers (in front of each line, separated by a tab) and returns the
	 * contentful lines in an arrayList
	 * 
	 * @param fileName
	 *            The file to process.
	 * @return OTsoftfile ArrayList of Strings containing the useful parts of
	 *         the file.
	 */
	public static ArrayList<String> getPrunedFile(String fileName) {

		// This commenting scheme seemed too permissive,
		// so we switched to HTML-style comments. --- Chris
		// Pattern pat1 = Pattern.compile("^[^\\'\\$\\/\\#]");
		String cmt = "/\\*\\*";
		Pattern comment = Pattern.compile(cmt);

		// Remove blank lines.
		Pattern blank = Pattern.compile("^\\s*$");

		// Phonologists tend to put things in angled brackets, and these
		// don't show up in HTML output --- they are treated as tags.
		String regex = "<([^>]*)>";
		Pattern falseTag = Pattern.compile(regex);

		ArrayList<String> OTsoftfile = new ArrayList<String>();

		File OTSoft = new File(fileName);
		if (OTSoft.exists() && OTSoft.isFile() && OTSoft.canRead()) {
			try {
				BufferedReader input = new BufferedReader(
						new FileReader(OTSoft));
				String line = null;
				int line_number = 0;
				while ((line = input.readLine()) != null) {
					line_number++;
					Matcher matcher1 = comment.matcher(line);
					Matcher matcher2 = blank.matcher(line);
					if (line.length() > 0 && matcher1.find() == false
							&& !matcher2.find()) {
						Matcher falseTagMatcher = falseTag.matcher(line);
						line = falseTagMatcher.replaceAll("&lt;$1&gt;");
						OTsoftfile.add(line_number + "\t" + line);
					}
				}
			} catch (FileNotFoundException ex) {
				ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			}

		} else {

			// System.out.println("File not found, or could not be read.");
		}

		return OTsoftfile;

	}

	/*
	 * Returns a randomly selected tableau, based on the frequencies of the
	 * winners
	 * 
	 * @return The randomly selected tableau
	 */

	public Tableau randomTableau() {
		double totalFrequency = 0;
		ArrayList<Candidate> winners = new ArrayList<Candidate>(); // keep track
																	// of the
																	// winners,
																	// in order

		//
		Iterator tableauxIter = this.getTableaux().iterator();
		while (tableauxIter.hasNext()) {
			Tableau currentTableau = (Tableau) tableauxIter.next();
			Iterator winnersIter = currentTableau.getWinners().iterator();
			while (winnersIter.hasNext()) {
				Candidate currentWinner = (Candidate) winnersIter.next();
				winners.add(currentWinner);
				totalFrequency += currentWinner.getFrequency();
			}
		}

		// Given a list of the winners, and their total frequency,
		// generate a number between 0 and the total frequency.
		// Run through the ArrayList of winners, subtracting the
		// frequency of each one from the random number, until it
		// reaches zero. The current winner's tableau will be returned.

		Random generator = new Random();
		Double choice = generator.nextDouble() * totalFrequency;// because the
																// random number
																// generated
																// will be
																// between 0 and
																// 1
		Iterator winnersIter = winners.iterator();
		while (winnersIter.hasNext()) {
			Candidate currentWinner = (Candidate) winnersIter.next();
			choice -= currentWinner.getFrequency();
			if (choice <= 0) {
				return currentWinner.getParent();
			}
		}

		return null; // The method should never get to this point.

	}
}

