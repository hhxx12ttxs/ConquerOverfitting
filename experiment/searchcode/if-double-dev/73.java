this.id = id;
this.now = now;
}

public void addDistribution(String id, double mean, double dev){
StdDev sd = new StdDev(mean, dev, r);
double mean;
double dev;
boolean hasMin = false; // true if a min is set
double min = Double.MIN_VALUE;

