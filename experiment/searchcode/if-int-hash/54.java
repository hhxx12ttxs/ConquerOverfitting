package hashTable;

public class HashMap {

private final static int TABLE_SIZE = 10;
public void put(int key, int value) {
int hash = (key % TABLE_SIZE);
while (table[hash] != null &amp;&amp; table[hash].getKey() != key)

