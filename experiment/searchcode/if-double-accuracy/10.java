* @return MGRS cell as string
*/
public String getAreaFromLatLong (double lat, double lng, int accuracy) {

MGRSPoint mgrsp = MGRSPoint.LLtoMGRS(new LatLonPoint.Double(lat, lng));

if (accuracy==1)
mgrsp.setAccuracy(MGRSPoint.ACCURACY_1_METER);

