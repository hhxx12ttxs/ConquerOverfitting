import java.io.*;      // required for Serialising objects

/**
 * Models Time for appointments
 */
public class Time implements Serializable {

	int hours, mins;

	public Time(int theHours, int theMins) {
		setHours(theHours);
		setMins(theMins);
	}
	
	/**
	 * Sets the attribute: hours
	 *
	 * @param theHours How many hours.
	 */
	public void setHours(int theHours) {
        hours = theHours;
    }

	/**
	 * Gets the attribute: hours
	 *
	 * @return int How many hours.
	 */
	public int getHours() {
        return hours;
    }
	
	/**
	 * Sets the attribute: mins
	 *
	 * @param theMins How many mins.
	 */
	public void setMins(int theMins) {
        mins = theMins;
    }

	/**
	 * Gets the attribute: mins
	 *
	 * @return int How many mins.
	 */
	public int getMins() {
        return mins;
    }
    
    public String toString() {
    	
    	String theMins = Integer.toString(mins);
    	String theHours = Integer.toString(hours);
    	
    	// Apply time format to hours.
		if (mins < 10) {
			theMins = "0" + theMins;
		}
		
		// Apply time format to minutes.
		if (hours < 10) {
			theHours = "0" + theHours;
		}
		
    	return theHours + ":" + theMins;
    }
}


