public class PriorityQueue<T> {

private int size;
private T ts[]; // ts[0] is not used
private Comparator<T> comparator;

public PriorityQueue(Class<T> clazz, int max, Comparator<T> comparator) {
this.comparator = comparator;
ts = Util.newArray(clazz, max + 1);

