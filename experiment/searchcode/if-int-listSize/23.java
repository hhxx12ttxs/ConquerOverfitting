public class SortBenchmark extends Benchmark {

private static int listSize = 0;
private static int count = 0;
ArrayList<Integer> list = new ArrayList<Integer>(listSize);
for (int i = 0; i < listSize; i++) {
list.add((int) (Math.random() * listSize));

