public class HashTableLP<Key, Value> {
private int M = 1001177;
private int size = 0;
private Key[] K = (Key[]) new Object[M];
if (key == null) { return null; }
for (int i = hash(key); K[i] != null; i = (i + 1) % M) {
if (K[i].equals(key)) {

