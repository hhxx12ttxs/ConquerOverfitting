package jgogears;

// TODO: Auto-generated Javadoc
/**
 * Class representing dan / kyu ranks. 30kyu to 1kyu and 1dan to 20dan. Classes are represented internally as a double.
 * Rank parsing and formatting are only supported for whole numbers.
 * 
 * @author syeates
 */

public final class Rank {

	/** The Constant SHODAN. */
	public final static Rank SHODAN = new Rank("1d");

	/** The Constant BEGINNER. */
	public final static Rank BEGINNER = new Rank("25k");

	/** The Constant ME. */
	public final static Rank ME = new Rank("12k");

	/** The Constant TESTING. */
	final static boolean TESTING = false;

	/** The Constant MAX_RANK. */
	final static double MAX_RANK = 80;

	/** The Constant MIN_RANK. */
	final static double MIN_RANK = 130;

	/** The Constant MAX_RATING. */
	final static double MAX_RATING = 1;

	/** The Constant MIN_RATING. */
	final static double MIN_RATING = 0;

	/**
	 * Check rank.
	 * 
	 * @param ranking
	 *            the ranking
	 * @return true, if successful
	 */
	static boolean checkRank(double ranking) {
		if (TESTING)
			if (ranking > MAX_RATING || ranking < MIN_RATING)
				throw new Error("bad ranking " + ranking);
		return true;
	}

	/**
	 * Check rating.
	 * 
	 * @param rating
	 *            the rating
	 * @return true, if successful
	 */
	static boolean checkRating(double rating) {
		if (TESTING)
			if (rating > MAX_RATING || rating < MIN_RATING)
				throw new Error("bad rating " + rating);
		return true;
	}

	/**
	 * Convert.
	 * 
	 * @param r
	 *            the r
	 * @return the int
	 */
	public static int convert(String r) {
		if (r == null || r.length() < 2)
			return (int) BEGINNER.getRank();
		r = r.toLowerCase();
		int offset = 0;
		while (r.charAt(offset) == ' ')
			offset++;
		StringBuffer number = new StringBuffer();
		while (Character.isDigit(r.charAt(offset))) {
			number.append(r.charAt(offset));
			offset++;
		}
		while (r.charAt(offset) == ' ')
			offset++;
		if (number.length() == 0)
			throw new Error();
		int num = Integer.parseInt(number.toString());
		if (r.charAt(offset) == 'k')
			return num + 100;
		else if (r.charAt(offset) == 'd')
			return 100 + 1 - num;
		else if (r.charAt(offset) == 'p')
			return 120 + 1 - num;
		else
			throw new Error(r);
	}

	/** The rank. */
	private double rank = 25;

	/**
	 * Instantiates a new rank.
	 */
	public Rank() {
		if (TESTING)
			checkRank(this.rank);
	}

	/**
	 * Instantiates a new rank.
	 * 
	 * @param r
	 *            the r
	 */
	public Rank(double r) {
		if (TESTING)
			checkRank(this.rank);
		this.rank = r;
		if (TESTING)
			checkRank(this.rank);
	}

	/**
	 * Instantiates a new rank.
	 * 
	 * @param r
	 *            the r
	 */
	public Rank(String r) {
		this.rank = convert(r);
		if (TESTING)
			checkRank(this.rank);
	}

	/**
	 * Gets the rank.
	 * 
	 * @return the rank
	 */
	public double getRank() {
		if (TESTING)
			checkRank(this.rank);
		return this.rank;
	}

	/**
	 * Gets the rating.
	 * 
	 * @return the rating
	 */
	public double getRating() {
		if (TESTING)
			checkRank(this.rank);
		if (TESTING)
			System.err.println(MAX_RANK + " " + MIN_RANK + " " + this.rank);
		double result = (MIN_RANK - this.rank) / (MIN_RANK - MAX_RANK);
		if (TESTING)
			checkRating(result);
		return result;
	}

	/**
	 * Sets the rank.
	 * 
	 * @param rank
	 *            the new rank
	 */
	public void setRank(double rank) {
		this.rank = rank;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (TESTING)
			checkRank(this.rank);
		StringBuffer result = new StringBuffer(10);
		if (this.rank <= 100)
			result.append((int) (-this.rank + 100 + 1)).append("dan");
		else
			result.append((int) (this.rank - 100)).append("kyu");
		return result.toString();
	}

}

