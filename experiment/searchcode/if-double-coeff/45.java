HashMap<Cluster, HashMap<Cluster, Double>> coeffResult;
if(jc) {
coeffResult = calcJc();
}
else if (oc) {
coeffResult = calcJcOc();
}
return coeffResult;
}

private HashMap<Cluster, HashMap<Cluster, Double>> calcJc() {

