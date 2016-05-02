package com.marketdata.marvin.midtier.framework.date;

public class Pattern {

	private String pattern;
	private String separator;
	private int patternLength;
	private Integer firstSeparatorOccurance;

	public Pattern(final String pattern) {
		this(pattern, null);
	}

	public Pattern(final String pattern, final String separator) {
		this.pattern = pattern;
		this.separator = separator;

		this.patternLength = this.pattern.length();
	}

	public Boolean confirmedFirstSeparator(final String str) {

		if (this.getSeparator() != null) {
			return str.indexOf(this.getSeparator()) == this.getFirstSeparatorOccurance();
		}

		return null;
	}

	// not threadsafe but does not matter as overhead of check is less than any sync
	public int getFirstSeparatorOccurance() {

		if (firstSeparatorOccurance == null && separator != null) {
			firstSeparatorOccurance = Integer.valueOf(this.getPattern().indexOf(this.getSeparator()));
		}

		return firstSeparatorOccurance.intValue();
	}

	public void setPattern(final String pattern) {
		this.pattern = pattern;
	}

	public String getPattern() {
		return pattern;
	}

	public void setSeparator(final String separator) {
		this.separator = separator;
	}

	public String getSeparator() {
		return separator;
	}

	public void setPatternLength(final int patternLength) {
		this.patternLength = patternLength;
	}

	public int getPatternLength() {
		return patternLength;
	}
}

