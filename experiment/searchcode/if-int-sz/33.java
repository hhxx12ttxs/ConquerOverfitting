public class WeightedQuickUnionAlgorithm extends QuickUnionAlgorithm {

protected int sz[];

public WeightedQuickUnionAlgorithm(int size) {
super(size);
sz = new int[size];
for (int i = 0; i < size; i++) {
sz[i] = 1;

