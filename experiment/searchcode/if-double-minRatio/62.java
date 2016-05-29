private AggGraph aggGraph;
private Column column;
private BitSetColumn filter;
private double minRatio = 0.0, maxRatio = 1.0;
this.type = type;
}

public void update(double minRatio, double maxRatio) {
this.minRatio = minRatio;

