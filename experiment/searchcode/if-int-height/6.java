public class ContainerWithWater {
public int maxArea(int[] height) {
int area = 0;
if (height == null)
return area;
for (int i = 0, j = height.length - 1; i < j; ) {

