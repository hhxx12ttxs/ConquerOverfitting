double[] xdata = x.getData();
double[] ydata = y.getData();
double max = Double.NEGATIVE_INFINITY;
count++;
double d = Math.abs(xdata[i] - ydata[i]);

if (d > max)
max = d;
}

return max;
}


public void setThreshold(double threshold) {
this.threshold = threshold;
}
}

