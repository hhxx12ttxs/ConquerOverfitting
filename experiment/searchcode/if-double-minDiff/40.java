return 0; // no activity was made
}

// returns the minimal difference between the data to each cluster
double minDiff = Double.MAX_VALUE;
diff = cluster.compare(data);
if (diff < minDiff){
minDiff = diff;

