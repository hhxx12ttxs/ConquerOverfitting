lc.setConstrainedValue(constrainedValue);
if (!con.getComparator().equals(FunctionComparator.Equality)) {
if (slackVar == null) {
lc.addVariable(slackVar, -1.0);
else
lc.addVariable(slackVar, 1.0);
}
else if (slackVar != null) {

