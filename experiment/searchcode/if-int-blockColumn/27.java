private final List<FieldListener> _listeners = new ArrayList<FieldListener>();

public Field(int base)
{
if(base < 2)
for(int blockColumn = 0; blockColumn < base; ++blockColumn)
{
final int columnStart = blockColumn * base;

