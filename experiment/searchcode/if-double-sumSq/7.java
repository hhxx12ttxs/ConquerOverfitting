protected int position = 0;
protected double sum = 0;
protected double sumSq = 0;
private int count = 0;

public DoubleArray()
this.position = toCopy.position;
}

public void add(double v)
{
if (values.length <= position)
{
values = Arrays.copyOf(values, values.length * 2);

