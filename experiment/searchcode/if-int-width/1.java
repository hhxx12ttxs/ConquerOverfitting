public class RugSizes {
public int rugCount(int area) {
int root=(int) Math.floor(Math.sqrt(area));
int cnt=0;
for (int width = 1; width <= root; width++)
if(area % width == 0 &amp;&amp; ( (width % 2 != 0) || ((area / width) % 2 != 0) || (width * width == area)))

