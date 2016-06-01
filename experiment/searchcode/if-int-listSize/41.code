private static final int DEFAULT_SIZE = 10;

private int maxSize;
private int listSize;
private int currentPosition;
private E[] listArray;
public void insert(E item) {

if(listSize >= maxSize) {
expandTheCapacity();
}

for(int i = listSize; i > currentPosition; i--) {

