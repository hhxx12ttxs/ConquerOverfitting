public class LayerComparator implements Comparator<Layer> {
public int compare(Layer l1, Layer l2) {
if (l1.zIndex < l2.zIndex) {
return -1;
} else
if (l1.zIndex > l2.zIndex) {
return 1;
} else {
System.err.println(&quot;Z-indices cannot be the same for &quot; + l1.name + &quot; and &quot; + l2.name + &quot;.&quot;);

