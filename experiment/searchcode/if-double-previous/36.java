DoubleNode newNode = new DoubleNode(x);
DoubleNode last = (DoubleNode)getLast();
if (last == null) {
setFirst(newNode);
DoubleNode first = (DoubleNode)getFirst();
if (first != null) {
DoubleNode previous = null;

