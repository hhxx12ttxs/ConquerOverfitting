public void delete(Entry<K, T> entry) {
EntryDNode<K,T> entryDNode = (EntryDNode<K,T>)entry;
if (entryDNode.previous!=null) {
} else {
first = entryDNode.next;
}
if (entryDNode.next!=null) {

