private String resultsDirInvy = &quot;results/invy&quot;;
private String resultsDirCov = &quot;results/cov&quot;;
private String resultsDirCovPlot = &quot;results/cov/plot&quot;;
try {

if (hasPropertyAndNotNull(&quot;breakpoint&quot;)) {
breakpoint = Boolean.valueOf(getProperty(&quot;breakpoint&quot;));

