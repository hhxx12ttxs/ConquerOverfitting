protected doubleLinkedNODE<E> nextElement;
protected doubleLinkedNODE<E> previousElement;

public doubleLinkedNODE(E v,doubleLinkedNODE<E> next,doubleLinkedNODE<E> previous){
data = v;
nextElement = next;
if (nextElement != null){

