private long sum;
private long sumSq;
private long avg;
private double stdDev;

public LongRunningStats(int field, long count, long sum, long sumSq) {
this.count = count;
this.sum = sum;
this.sumSq = sumSq;
}

public LongRunningStats(int field, long avg, double stdDev) {

