((RecursiveDoubleLinkedListImpl<T>) next).setPrevious(this);

if(previous == null) {
setPrevious(new RecursiveDoubleLinkedListImpl<T>());
setNext(next.getNext());
((RecursiveDoubleLinkedListImpl<T>) next).setPrevious(this);
}

} else if (next.getData() != null){

