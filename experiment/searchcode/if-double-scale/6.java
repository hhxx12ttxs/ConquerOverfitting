protected final NoiseGenerator[] octaves;
protected double xScale = 1.0D;
protected double yScale = 1.0D;
protected double zScale = 1.0D;
this.octaves = octaves;
}

public void setScale(double scale) {
setXScale(scale);
setYScale(scale);
setZScale(scale);

