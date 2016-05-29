private CyclicItem firstItem;

public boolean add(CyclicItem item) {

if (item == null) {
throw new NullPointerException();
}

if (firstItem == null) {

