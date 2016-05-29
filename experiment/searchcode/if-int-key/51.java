public class HashCodeUtil {

public static int calculateIntHashCode(int key) {
key = ~key + (key << 15); // key = (key << 15) - key - 1;
key = key ^ (key >>> 16);
return key;
}

public static int calculateLongHashCode(long key) {

key = (~key) + (key << 21); // key = (key << 21) - key - 1;

