double[] colMeans = columnMeans(x, nrow, ncol, hasNA);
for (int i = 0; i < ncol ; i++) {
if (hasNA[i]) {
for (int i = 0; i < ncol; i++) {
if (!hasNA[i]) {
for (int j = 0; j < i; j++) {

