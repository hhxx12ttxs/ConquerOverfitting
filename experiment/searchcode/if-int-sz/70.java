package unionfind;

public class WeightedQuickUnion {
private int[] id;
private int[] sz;	//Keeps track of the size of the tree

public WeightedQuickUnion(int n) {
id = new int[n];
sz = new int[n];

