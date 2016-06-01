public boolean empty(){
return back == null;
}

public Int enqueue(E data){
Node<E> tmp = new Node<E>(data);
if (empty()){
front = tmp;
back = tmp;
} else {
back.setNext(tmp);
back = tmp;
}
}

