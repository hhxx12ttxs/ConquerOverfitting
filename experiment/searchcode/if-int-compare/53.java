this.compareInfo = compareInfo;
}

public Color getDirectionColor() {
Boolean evaluate = compareInfo.getEvaluate();
if (evaluate == null) {
} else {
return Color.red;
}
}

public String getCompare() {
int compare = compareInfo.getResult();

