int bucketIndex = (int) (key &amp; tableHighestIndex);

if (numberKeys[bucketIndex] > 0)
{
int keyIndex = CollectionHelper.binarySearch(keys[bucketIndex], key, numberKeys[bucketIndex]);
Object oldValue = local_values[keyIndex];

int highest_index = local_values.length - 1;

if (keyIndex < highest_index)

