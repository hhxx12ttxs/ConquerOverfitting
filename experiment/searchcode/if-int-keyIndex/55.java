int bucketIndex = (int) (key &amp; tableHighestIndex);

if (sizes[bucketIndex] > 0)
{
int keyIndex = CollectionHelper.binarySearch(keys[bucketIndex], key, sizes[bucketIndex]);

