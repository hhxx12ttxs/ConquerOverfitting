private int size;

@Override
public void push(T el) {
Node<T> nod = new Node<>();
nod.el = el;
if (tail == null) {
nod.el = el;
if (tail == null) {
head = nod;
tail = nod;
size++;
} else {
head.right = nod;

