double minratio = 0;
double maxratio = model.getMaxVal();
if (getProperties().MaxRatio > 0) {
boolean drawothers = props.OtherHorizontalLines;

if (logscale) {
for (double ratio = 1; ratio < maxratio; ratio *= 2) {

