private E[] internalArray;
private int listSize = 0;

public CustomList(int initialCapacity) {
this.internalArray = (E[]) new Object[initialCapacity];
public E remove(int index) {
if (index >= 0 &amp;&amp; index < listSize) {
System.arraycopy(internalArray, index + 1, internalArray, index, listSize - index - 1);

