int ncols = getColumns();

if (ncomponents == 0) {
return;
}
int fakeNum = Math.max(ncomponents, nrows*ncols);
for (int r = 0, y = insets.top ; r < nrows ; r++, y += h + getVgap()) {
int i = (vertical ? c * nrows + r : r * ncols + c);
if (i < ncomponents) {

