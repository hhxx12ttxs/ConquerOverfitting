/** The next element. */
protected DoubleNode<E> nextElement;

/** The previous element. */
protected DoubleNode<E> previousElement;
public DoubleNode(E v,DoubleNode<E> next,DoubleNode<E> previous)
{
data = v;
nextElement = next;
if (nextElement != null)

