key ^= (key >>> 11);
key += (key << 6);
key ^= (key >>> 22);
return (int) key;
}

public static final int longToBucket(long key, int buckets) {
key = (~key) + (key << 18); // key = (key << 18) - key - 1;

