public AllocationEntry schedule(Job job) {
double minRatio = Double.MAX_VALUE;
long size = ((SWFJob)job).getRequestedNumberOfProcessors();
double ratio = (size / (effi*mi)) + this.minPl.get(s).doubleValue();
if(ratio < minRatio &amp;&amp; size <= mi){

