import java.util.*;

/**
 * TreapSet class makes use of ideas from AVL trees and Heaps to provide some
 * balance in a binary search tree.
 **/
public class TreapSet<E> implements SortedSet
{
	int size;	//size of treap
	TreapNode<E> root;	//root of treap
	Comparator<? super E> comp;	//Comparator
	int count;	
	Random rand; //Random number generator for priorities in treapNodes

	/**
	 * Default constructor
	 **/
	public TreapSet() {
		size = 0;
		root = null;
		comp = null;
		rand = new Random();
	}

	/**
	 * Constructor takes a comparator if keys to be used do not implement comparable
	 * @param  c comparator to be used for comparisons
	 **/
	public TreapSet(Comparator<? super E> c) {
		size = 0;
		root = null;
		comp = c;
		rand = new Random();
	}

	/**
	 * Returns the root key of treap
	 * @return key of root
	 **/
	public E getRoot()
	{
		return root.getKey();
	}

	/**
	 * Inserts an element into the treap.
	 *
	 * @param  o  the object to be added to the treap
	 * @return  true if successful and false if item is already in treap
	 **/
	public boolean add(Object o) {
		boolean successful = true; //assume key will be added
		int preSize = size; //Size to check if a node was added
		E key = (E)o; //cast object to generic
		if (comp != null) {
			if (root == null) root = new TreapNode<E>(key, null, null);
			else if (comp.compare(root.getKey(), key) > 0) {
				root.setLeft(TreapInsert(key, root.getLeft()));
				if (root.getLeft().getPriority() > root.getPriority()) root = rotateRight(root, root.getLeft());			
			}
			else if (comp.compare(root.getKey(), key) < 0) {
				root.setRight(TreapInsert(key, root.getRight()));
				if (root.getRight().getPriority() > root.getPriority()) root = rotateLeft(root, root.getRight());
				
			}
			else {
				size--; //decrement size 
			}
		}
		else {
			if (root == null) root = new TreapNode<E>(key, null, null);
			else if (((Comparable)root.getKey()).compareTo((Comparable)key) > 0) {
				root.setLeft(TreapInsert(key, root.getLeft()));
				if (root.getLeft().getPriority() > root.getPriority()) root = rotateRight(root, root.getLeft());			
			}
			else if (((Comparable)root.getKey()).compareTo((Comparable)key)  < 0) {
				root.setRight(TreapInsert(key, root.getRight()));
				if (root.getRight().getPriority() > root.getPriority()) root = rotateLeft(root, root.getRight());
				
			}
			else {
				size--; //decrement size 
			}

		}

		size++;
		if (size == preSize) successful = false; //Checks if size has changed to determine if object was inserted
		//System.out.println(root.getKey());
		return successful;
	}

	//Helper method
	private TreapNode<E> TreapInsert(E key, TreapNode<E> node) {
		boolean successful = true; //assume key will be added

		if (comp == null) {
			if (node == null) node = new TreapNode<E>(key, null, null);
			else if (((Comparable)node.getKey()).compareTo((Comparable)key) > 0) {
				node.setLeft(TreapInsert(key, node.getLeft()));
				if (node.getLeft().getPriority() > node.getPriority()) node = rotateRight(node, node.getLeft());
			}
			else if (((Comparable)node.getKey()).compareTo((Comparable)key)  < 0) {
				node.setRight(TreapInsert(key, node.getRight()));
				if (node.getRight().getPriority() > node.getPriority()) node = rotateLeft(node, node.getRight());
			}
			else {
				size--;
			}
		}
		else {
			if (node == null) node = new TreapNode<E>(key, null, null);
			else if (comp.compare(node.getKey(), key) > 0) {
				node.setLeft(TreapInsert(key, node.getLeft()));
				if (node.getLeft().getPriority() > node.getPriority()) node = rotateRight(node, node.getLeft());
			}
			else if (comp.compare(node.getKey(), key) < 0) {
				node.setRight(TreapInsert(key, node.getRight()));
				if (node.getRight().getPriority() > node.getPriority()) node = rotateLeft(node, node.getRight());
			}
			else {
				size--;
			}
		}

		return node;
	}

	private TreapNode<E> rotateLeft(TreapNode<E> node, TreapNode<E> rChild) {
		//System.out.println("Rotating Left");
		node.setRight(rChild.getLeft());
		rChild.setLeft(node);
		return rChild;
	}

	private TreapNode<E> rotateRight(TreapNode<E> node, TreapNode<E> lChild) {
	//	System.out.println("Rotating Right");
		node.setLeft(lChild.getRight());
		lChild.setRight(node);
		return lChild;
	}

	/**
	 * Clears the entire treap
	 **/
	public void clear() {
		root = null;
		size = 0;
	}

	/**
	 * Checks if an object is in the treap
	 *
	 * @param  o  object to search for in treap
	 * @return  true if found
	 **/
	public boolean contains(Object o) {
		E key = (E)o; //Cast object to generic type
		boolean found = false;
		int compVal;

		TreapNode<E> next = root;
		while (next != null && !found) {
			if (comp == null) {
				if (((Comparable)next.getKey()).compareTo((Comparable)key) == 0) found = true;
				else if (((Comparable)next.getKey()).compareTo((Comparable)key) < 0) next = next.getRight();
				else if (((Comparable)next.getKey()).compareTo((Comparable)key) > 0) next = next.getLeft();
			}
			else {
				compVal = comp.compare(next.getKey(), key); //only one call to compare for benchmarking purposes
				if (compVal == 0) found = true;
				else if (compVal < 0) next = next.getRight();
				else if (compVal > 0) next = next.getLeft();
			}
		}

		return found;
	}

	/**
	 * Checks if tree is empty
	 *
	 * @return  true if treap is empty, false if not empty
	 **/
	public boolean isEmpty(){
		return (root == null);
	}

	/**
	 * Removes an element from the set.
	 *
	 * @param  o  object to remove from the set
	 * @return  true if object was in set
	 **/
	public boolean remove(Object o) {
		E key = (E)o; //Cast object to generic type
		boolean found = false;
		int preSize = size;
		
		root = treapDelete(key, root);
		size--;
		if (size != preSize) found = true;

		return found;
	}

	private TreapNode<E> treapDelete(E key, TreapNode<E> node) {
		if (node != null) {
			if (comp == null) {
				if (((Comparable)node.getKey()).compareTo((Comparable)key) > 0) node.setLeft(treapDelete(key, node.getLeft()));
				else if (((Comparable)node.getKey()).compareTo((Comparable)key) < 0) node.setRight(treapDelete(key, node.getRight()));
				else node = rootDelete(node);
			}
			else {
				if (comp.compare(node.getKey(), key) > 0) node.setLeft(treapDelete(key, node.getLeft()));
				else if (comp.compare(node.getKey(), key) < 0) node.setRight(treapDelete(key, node.getRight()));
				else node = rootDelete(node);
			}
		}
		else size++;
		return node;
	}

	private TreapNode<E> rootDelete(TreapNode<E> node) {
		TreapNode<E> tempNode;
		if (node.isLeaf()) {
		       node = null;
		       tempNode = null;
		}
		else if (node.getRight() == null) {
			tempNode = rotateRight(node, node.getLeft());
			treapDelete(node.getKey(), tempNode);
		}
		else if (node.getLeft() == null) {
			tempNode = rotateLeft(node, node.getRight());
			treapDelete(node.getKey(), tempNode);
		}
		else if ((node.getLeft()).getPriority() > (node.getRight()).getPriority()) {
			tempNode = rotateRight(node, node.getLeft());
			treapDelete(node.getKey(), tempNode);
		}
		else {
			tempNode = rotateLeft(node, node.getRight());
			treapDelete(node.getKey(), tempNode);
		}

		return tempNode;
	}
	
	/**
	 * Returns the number of nodes in the treap.
	 *
	 * @return  size of the treap
	 **/
	public int size() {
		return size;
	}
	
	/**
	 * Returns an iterator of the elements in the set. Elements are returned in ascending order.
	 *
	 * @return  iterator of the elements in the set
	 **/
	public Iterator<E> iterator() {
		return new TreapIterator<E>();
	}

	/**
	 * Returns an array of the elements in the set.
	 * 
	 * @return  an array of the elements in the set
	 **/
	public Object[] toArray() {
		Object[] anArray = new Object[size];
		count = 0;
		//Iterate over tree adding keys to array
		traverse(anArray, root);

		return anArray;
	}

	private void traverse(Object[] anArray, TreapNode<E> node) {
		if(node.getLeft() != null)
			traverse(anArray, node.getLeft());
		anArray[count] = node.getKey();
		count++;
		if(node.getRight() != null)
			traverse(anArray, node.getRight());
	}

	/**
	 * Returns the comparator.
	 * @return  the comparator being used by the TreapSet
	 **/
	public Comparator<? super E> comparator() {
		return comp;
	}
	
	/**
	 * Returns the lowest element in the set
	 *
	 * @return  the lowest element in the set
	 **/
	public E first() throws NoSuchElementException {		
		if (root == null) throw new NoSuchElementException();
		else {
			TreapNode<E> next = root;
			while (next.getLeft() != null) {
				next = next.getLeft();
			}
			return next.getKey();
		}
	}

	/**
	 * Returns the highest element in the set
	 *
	 * @return  the highest element in the set
	 **/
	public E last() throws NoSuchElementException {
		if (root == null) throw new NoSuchElementException();
		else {
			TreapNode<E> next = root;
			while (next.getRight() != null) {
				next = next.getRight();
			}
			return next.getKey();
		}
	}

	public SortedSet headSet(Object toElement) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	public SortedSet subSet(Object fromElement, Object toElement) throws UnsupportedOperationException{
		throw new UnsupportedOperationException();
	}

	public SortedSet tailSet(Object fromElement) throws UnsupportedOperationException{
		throw new UnsupportedOperationException();
	}

	public boolean addAll(Collection c) throws UnsupportedOperationException{
		throw new UnsupportedOperationException();
	}

	public boolean containsAll(Collection c) throws UnsupportedOperationException{
		throw new UnsupportedOperationException();
	}

	public boolean equals(Object o) throws UnsupportedOperationException{
		throw new UnsupportedOperationException();
	}

	public int hashCoded() throws UnsupportedOperationException{
		throw new UnsupportedOperationException();
	}

	public boolean removeAll(Collection c) throws UnsupportedOperationException{
		throw new UnsupportedOperationException();
	}

	public boolean retainAll(Collection c) throws UnsupportedOperationException{
		throw new UnsupportedOperationException();
	}

	public Object[] toArray(Object[] a) throws UnsupportedOperationException{
		throw new UnsupportedOperationException();
	}

	/**
	 * TreapNode inner class for use of TreapSet
	 **/
	public class TreapNode<E>
	{
		private E key;
		int priority;
		TreapNode<E> left;
		TreapNode<E> right;
		
		/**
		 * TreapNode constructor
		 **/
		public TreapNode(E k, TreapNode<E> l, TreapNode<E> r) {
			key = k;
			priority = rand.nextInt();
			//System.out.println("Priority = " + priority);
			left = l;
			right = r;
		}
		
		/**
		 * Returns key
		 * @return key
		 **/
		public E getKey() {
			return key;
		}
		
		/**
		 * Returns the priority of the node
		 * @return  node priority
		 **/
		public int getPriority() {
			return priority;
		}

		/**
		 * Returns the left child of the node
		 * @return  the left child
		 **/
		public TreapNode<E> getLeft() {
			return left;
		}

		/**
		 * Returns the right child of the node
		 * @return  the right child
		 **/
		public TreapNode<E> getRight() {
			return right;
		}
		
		/**
		 * Checks if node is a leaf
		 * @return true if node is a leaf
		 **/
		public boolean isLeaf() {
			return (this.getLeft() == null && this.getRight() == null);
		}

		/**
		 * Sets the right child
		 * @param newRight the new right child
		 **/
		public void setRight(TreapNode<E> newRight) {
			right = newRight;
		}

		/**
		 * Sets the left child
		 * @param newLeft the new left child
		 **/
		public void setLeft(TreapNode<E> newLeft) {
			left = newLeft;
		}
	}

	/**
	 * TreapIterator
	 **/
	public class TreapIterator<E> implements Iterator<E>
	{
		Stack<TreapNode<E>> iter;
		
		/**
		 * Default Constructor for TreapIterator
		 **/
		TreapIterator() {
			iter = new Stack<TreapNode<E>>();

			iter.push((TreapNode<E>)root);

			TreapNode<E> current = iter.peek();
			while(current.getLeft() != null)
			{
				iter.push(current.getLeft());
				current = current.getLeft();
			}

		}

		/**
		 * Determines if entire TreapSet has been iterated over
		 * @return  true if their is another value 
		 **/
		public boolean hasNext() {
			return !iter.empty(); 
		}

		/**
		 * Returns the next value in the Treap
		 * @return next key in the treap
		 **/
		public E next() {
			TreapNode<E> current, retVal;
			current = iter.pop();
			retVal = current;

			if(current.getRight() != null)
			{
				iter.push(current.getRight());
				current = current.getRight();
			
				while(current.getLeft() != null)
				{
					iter.push(current.getLeft());
					current = current.getLeft();
				}
			}
			return retVal.getKey();
		}

		public void remove() throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}
	}
}

