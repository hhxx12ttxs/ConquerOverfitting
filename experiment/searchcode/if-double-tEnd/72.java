Aggregator agg = new Aggregator(timestamps, values);
return agg.getAggregates(tStart, tEnd);
}

double getPercentile(long tStart, long tEnd, double percentile) throws RrdException {
return agg.getPercentile(tStart, tEnd, percentile, false);
}

double getPercentile(long tStart, long tEnd, double percentile, boolean includenan) throws RrdException {

