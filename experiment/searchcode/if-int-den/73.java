public class ComputeChange {

public static int makeChange(int sum, ArrayList<Integer> den) {
if (sum == 0) {
return 1;
}
for (int i = 0; i < den.size();) {
if (sum < den.get(i) || den.get(i) <= 0) {

