((DoubleLinkedListNode<T>)head).previous.next = head;
((DoubleLinkedListNode<T>)head.next).previous = (DoubleLinkedListNode<T>)head;

if (head.next.isNIL())
last = (DoubleLinkedListNode<T>)head;
((DoubleLinkedListNode<T>)head.next).previous = ((DoubleLinkedListNode<T>)head).previous;
head = newHead;
if (head.isNIL())
last = (DoubleLinkedListNode<T>)head;

