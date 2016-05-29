public class ColumnedLine {
protected String text;
protected int number;
protected String[] columns;
if (columns == null || columns.length == 0) return &quot;&quot;;
return StringUtils.join(columns, &quot;\t&quot;);
}

public String toString(int replaceIndex, String replace) {

