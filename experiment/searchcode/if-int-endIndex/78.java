public  static <T extends Comparable<? super T>> void stoogeSort(T[] source,int startIndex,int endIndex)
{
if (source[endIndex].compareTo(source[startIndex]) < 0)
SwapUtil.swap(source, startIndex, endIndex);

if ((endIndex - startIndex + 1) >= 3)
{
int t = (endIndex - startIndex + 1)/3;

