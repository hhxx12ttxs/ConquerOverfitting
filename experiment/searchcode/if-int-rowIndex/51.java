import java.util.*;

public class Q119PascalTriangleII {
public List<Integer> getRow(int rowIndex) {
rowIndex++;
List<Integer> ans = new ArrayList<Integer>();
if (rowIndex < 1) {
return ans;

