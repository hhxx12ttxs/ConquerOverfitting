double b = metric.getCost(stepB) + getSummedMetricTo(stepB);
if (a < b) {
return -1;
int compareNode = this.myComparator.compare(stepA.getStartNode(), stepB.getStartNode());
if (compareNode == 0) {

