public class LRUCache {

private int ts;
private int capacity;
private PriorityQueue<TS> heap;
class TSComparator implements Comparator<TS> {
@Override
public int compare(TS x, TS y) {
if(x.ts<y.ts) return -1;

