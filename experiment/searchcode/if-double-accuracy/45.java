private static Logger logger = LogManager.getLogger( GeoCoordinates.class.getName());

private Double	latitude;
private Double	longitude;
private Double	accuracy;  // Accuracy of location measured in meters
* @param latitude
* @param longitude
*/
public GeoCoordinates( Double latitude, Double longitude, Double accuracy ) {

