public E get(Point p, double r) {
BasicEntry<E> basicEntry = getEntry(p, r);
return basicEntry == null ? null : basicEntry.get();
}

protected BasicEntry<E> getEntry(Point p, double r) {

