import java.util.List;

public class Solution {
public static List<Integer> getRow(int rowIndex) {
List<Integer> ret = new ArrayList<Integer>();

if (rowIndex < 0)
return ret;
ret.add(1);
if (rowIndex == 0)

