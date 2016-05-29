package leetcode;

public class UniqueBST {
public int numTrees(int n) {
if (n == 0 || n == 1) return 1;

int[] num = new int[n + 1];
num[0] = 1;

