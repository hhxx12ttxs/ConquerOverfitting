class CombinationIterable<T> implements Iterable<T[]>
{
private T input[];
private int sampleSize;
private int numElements;

public CombinationIterable(int sampleSize, T... input)
{
this.sampleSize = sampleSize;

