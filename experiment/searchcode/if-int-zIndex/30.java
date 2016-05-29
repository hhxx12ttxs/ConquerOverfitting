public class RegionComparator implements Comparator<Region> {
public int compare(Region r1, Region r2) {
if (r1.zIndex < r2.zIndex) {
return -1;
} else
if (r1.zIndex > r2.zIndex) {
return 1;
} else {
System.err.println(&quot;Z-indices cannot be the same for &quot; + r1.toString() + &quot; and &quot; + r2.toString() + &quot;.&quot;);

