int index = distanceToIndex(x);
return value(index);
}
public double value(int index) {
if(index<0) return Y[0];
if(index>=Y.length) return 0;
return Y[index];
}

public void setValue(int index, double y) {

