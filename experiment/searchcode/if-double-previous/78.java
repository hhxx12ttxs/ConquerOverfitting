protected RecursiveDoubleLinkedListImpl<T> previous;


public RecursiveDoubleLinkedListImpl() {

}

public RecursiveDoubleLinkedListImpl(T data, RecursiveSingleLinkedListImpl<T> next, RecursiveDoubleLinkedListImpl<T> previous) {
NIL.setPrevious(this);
if(getPrevious() == null){
NIL = new RecursiveDoubleLinkedListImpl<T>();
NIL.setNext(this);

