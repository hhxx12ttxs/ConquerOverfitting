hookSet.remove(panel);
if(hookSet.isEmpty()) {
close();
}
}
}
private void addSnapPos(double time, Long pos) {
public double[] getTimeList() {
synchronized (this) {
Set<Double> set = snapMap.keySet();
int size = set.size();
if(savedBeginPos!=nowBeginPos &amp;&amp; nowBeginPos!=null &amp;&amp; !set.contains(nowTime)) {

