private List<Series> seriesList;

/**
* Some operations are invalid if a SeriesGroup object is empty
* (i.e., contains no series). this exception is thrown in cases
* Check for an empty SeriesGroups
*
* @return true if the object contains no series, false otherwise
*/
public boolean isEmpty() {

