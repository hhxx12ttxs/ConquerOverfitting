List<Double> l = iterableList(a).sort(doubleOrd);
int len = l.length();
P2<List<Double>, List<Double>> split = l.splitAt(len / 2);

if (len % 2 == 0) {

