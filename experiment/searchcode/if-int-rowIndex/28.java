public class Pascals_Triangle2 {
public static List<Integer> getRow(int rowIndex) {
if(rowIndex < 0) return null;
List<Integer> list = new ArrayList<Integer>();
for (int i = 0; i < rowIndex+2; i++) {

