public void addDirectly(Range range, P1<Range> job) {

RangeJob lastJob;

if (list.size() > 0 &amp;&amp; (lastJob = list.get(list.size() - 1)).job == job &amp;&amp; lastJob.range.getTo().equals(range.getFrom())) {
int index = Cols.searchIndexedBinary(list, RangeJob.rangeF, rangeJob.range);
if (index < 0) {
index = -index - 1;
}

list.add(index, rangeJob);

