//TODO: maybe this should really be part of dataset.getRows()...
int i = 0;
for (String s : rowDimension.getCategory()) {
for (Dimension dimension : dimensions) {
boolean isRow = rowDimension.equals(dimension.getId());
if (dimension.isRequired() &amp;&amp; !isRow) {

