public void addSeries(XYIntervalSeries series) {
if (series == null) {
throw new IllegalArgumentException(&quot;Null &#39;series&#39; argument.&quot;);
return this.data.size();
}


public XYIntervalSeries getSeries(int series) {
if ((series < 0) || (series >= getSeriesCount())) {

