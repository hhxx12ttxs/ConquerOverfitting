private String resultsDirInvy = &quot;delly_results/invy&quot;;
private String resultsDirCov = &quot;delly_results/cov&quot;;
private String resultsDirCovPlot = &quot;delly_results/cov/plot&quot;;
String inputBamPathTumor = null;
String inputBamPathGerm = null;

String[] runIDs = null;

private void init() {
try {

if (hasPropertyAndNotNull(&quot;breakpoint&quot;)) {

