* Created by hjh on 15-12-29.
*/
public class FirstEstimationRBolt extends JedisRichBolt {
Estimator estimator;
double paraEst;
String zoneid = tuple.getStringByField(StormUtils.STORM.FIELDS.ZONE_ID);
firstEstimate(caseid, zoneid);
collector.ack(tuple);

