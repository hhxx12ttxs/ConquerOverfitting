public class EDDataAccess {

String defaultLocation;
int defaultYear;
Hashtable dataSet;
Hashtable absoluteOverride;
private static Logger logger = Logger.getLogger(&quot;com.pb.tlumip.ed&quot;);

EDDataAccess(int defaultYear, String defaultLocation, String absoluteLocation, String marginalLocation) {

