package org.knuth.chkupdate;

/**
 * Used to compare two version and see which one
 *  is the newer one.
 * @author Lukas Knuth
 *
 */
public class Version {
	
	/** RegEx used to parse the Version-String */
	private final String CHAR_REGEX = "[\\w&&[^0-9]]";
	/** The RegEx used to split the Version-String up */
	private final String SPLIT_REGEX = "\\.";
	
	/** Splited version-numbers */
	private int[] numbers;
	
	/**
	 * Creates a new Version-Object by parsing the given
	 *  Version String.
	 * @param version_str The Version-String to parse.
	 */
	public Version(String version_str){
		parseVersion(version_str);
	}
	
	/**
	 * Splits up the version-string by the "."-characters
	 *  and creates a version-level-array.
	 * @param version_str The version-String to parse.
	 */
	private void parseVersion(String version_str){
		// Use RegEx to delete all unnecessary characters:
		String tmp = version_str.replaceAll(CHAR_REGEX, "");
		// Split by the "."
		String[] numbers_str = tmp.split(SPLIT_REGEX);
		this.numbers = new int[numbers_str.length];
		for (int i = 0; i < numbers_str.length; i++){
			this.numbers[i] = Integer.parseInt(numbers_str[i]);
		}
	}
	
	/**
	 * Checks if this version is higher (newer) then the
	 *  given one.
	 * @param version The Version to check against this
	 *  Version.
	 * @return "true" if this Version is higher (newer)
	 *  otherwise "false".
	 */
	public boolean isNewerThen(Version version){
		int[] other = version.numbers;
		version.numbers = null;
		int current;
		int remote;
		// Check which one is longer:
		int iterations;
		if (this.numbers.length > other.length){
			iterations = this.numbers.length;
		} else if (this.numbers.length < other.length){
			iterations = other.length;
		} else iterations = other.length;
		// Check for the higher one:
		for (int i = 0; i < iterations; i++){
			// Check if long enaught
			if (i >= this.numbers.length){
				current = 0;
			} else current = this.numbers[i];
			if (i >= other.length){
				remote = 0;
			} else remote = other[i];
			// Check which one is newer
			if (current < remote) return false;
			else if (current > remote) return true;
		}
		return true;
	}
	
	/**
	 * Returns the version-String for this "Version"-
	 *  Object.
	 */
	@Override
	public String toString(){
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < numbers.length; i++){
			b.append(numbers[i]);
			if (i < (numbers.length -1)){
				b.append(".");
			}
		}
		return b.toString();
	}

}

