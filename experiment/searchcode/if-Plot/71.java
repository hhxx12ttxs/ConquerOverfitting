for (Plot exc : exclude) {
if (plot.equals(exc)) {
allowed = false;
if (!allowed) {
continue;
}
double dist = plot.edgeDistance(location);
if (dist < closestDist) {

