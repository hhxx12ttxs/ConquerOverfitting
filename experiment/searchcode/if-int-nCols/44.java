int nrows = getRows();
int ncols = getColumns();
if (nrows > 0) {
ncols = (ncomponents + nrows - 1) / nrows;
for (int r = 0, y = insets.top; r < nrows; r++) {
int i = r * ncols + c;
if (i < ncomponents) {

