private final BufferedRandomAccessFile file;

private int curRangeIndex;
private Deque<IColumn> blockColumns = new ArrayDeque<IColumn>();
public IColumn pollColumn()
{
IColumn column = blockColumns.poll();
if (column == null)

