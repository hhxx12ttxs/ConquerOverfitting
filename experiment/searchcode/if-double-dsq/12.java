private void RecurseForce(ArrayIndexedGraph octree, ArrayIndexedNode nn, double dsq,
double epssq) { // recursively walks the tree to compute the force on a body
drz = n.posz - posz;
drsq = drx * drx + dry * dry + drz * drz;
if (drsq < dsq) {
if (!(n instanceof OctTreeLeafNodeData)) { // n is a cell

