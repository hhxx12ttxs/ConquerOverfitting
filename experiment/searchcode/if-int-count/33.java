public class UniqueBinarySearchTrees {
public int numTrees(int n) {
if (n < 2)
return 1;
int[] count = new int[n + 1];
count[0] = 1;
count[1] = 1;
// count[2] = count[0] * count[1]

