Object[] plotNames = this.plotDatabase.getAllPlotNames();
if (plotNames != null) {
for (Object plotName : plotNames) {
if (this.plotDatabase.getPlotByName(plotName.toString()).owner.equalsIgnoreCase(owner)) {
if (plots == null) {

