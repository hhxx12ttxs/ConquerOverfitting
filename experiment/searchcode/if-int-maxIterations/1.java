List<K> list;

Random rand;

int nbIterations = 0;
int maxIterations;

public ListRandomInfiniteIterator(List<K> l) {
public ListRandomInfiniteIterator(List<K> l, int maxIterations) {
list = l;
this.maxIterations = maxIterations;

