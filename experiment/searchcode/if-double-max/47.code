private double max;

public ContinueFeature(String name) {
super(name);
min = Double.MAX_VALUE;
max = -1;
public void setMax(double max){
if(max > this.max)
this.max = max;
}

public double getScaled(double value){
return (value - min) / (max - min);
}
}

