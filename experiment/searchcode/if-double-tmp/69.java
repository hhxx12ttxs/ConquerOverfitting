List<Pair<Double, Double>> newList = new ArrayList<Pair<Double, Double>>();

Pair<Double, Double> tmp = new Pair(1, -125);
//represents the linear formula of the building top, and check if tmp.second is over or under.
for (Pair<Double, Double> azimuthRanges : skys.keySet()) {

if(tmp.getFirst()>=azimuthRanges.getFirst() &amp;&amp; tmp.getFirst()<=azimuthRanges.getSecond()){

