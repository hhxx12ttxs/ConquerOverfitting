private String predictionFile;
private long fileLineNo;
private int nhDepth = 0;
private int nhDensity = 0;
public int getNhDensity() {
if (classNames!= null) {
nhDensity = classNames.size();
}
return nhDensity;

