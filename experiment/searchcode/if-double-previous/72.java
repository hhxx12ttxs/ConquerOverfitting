protected DoubleLinkedNode<E> previousElement;

public DoubleLinkedNode(E object, DoubleLinkedNode<E> next, DoubleLinkedNode<E> previous){
data= object;
nextElement=next;
if(nextElement != null){

