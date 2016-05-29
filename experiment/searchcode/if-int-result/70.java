public class ListAlgo {
public static int[] getTwoMin(ListInt list) {
int[] result = new int[2];
result[0] = list.get(0);
result[1] = list.get(1);
if (result[0] > result[1]) {

