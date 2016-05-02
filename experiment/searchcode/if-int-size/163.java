package org.ooc.frontend.model;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.ooc.frontend.Visitor;
import org.ooc.frontend.model.tokens.Token;

public class NodeList<T extends Node> extends Node implements Iterable<T>, Collection<T> {
	
	public static interface AddListener<T extends Node> {
		
		public void onAdd(NodeList<T> list, T element);
		
	}
	
	private T[] nodes;
	private int size;
	private AddListener<T> addListener = null;
	
	public NodeList() {
		this(Token.defaultToken);
	}
	
	public NodeList(Token startToken) {
		this(5, startToken);
	}
	
	public void addAddListener(AddListener<T> addListener) {
		this.addListener = addListener;
	}
	
	@SuppressWarnings("unchecked")
	public NodeList(int initialCapacity, Token startToken) {
		super(startToken);
		nodes = (T[]) new Node[initialCapacity];
		size = 0;
	}

	@SuppressWarnings("unchecked")
	private void realloc() {
		Object[] oldNodes = nodes;
		nodes = (T[]) new Node[(size * 3) / 2 + 1];
		System.arraycopy(oldNodes, 0, nodes, 0, size);
	}
	
	@SuppressWarnings("unchecked")
	private void ensureCapacity(int minCapacity) {
		if(minCapacity > nodes.length) {
			Object[] oldNodes = nodes;
			nodes = (T[]) new Node[minCapacity];
			System.arraycopy(oldNodes, 0, nodes, 0, size);
		}
	}
	
	public boolean add(T element) {
		if(size >= nodes.length) realloc();
		nodes[size++] = element;
		if(addListener != null) addListener.onAdd(this, element);
		return true;
	}

	public void add(int index, T element) {
		if(size + 1 >= nodes.length) realloc();
		System.arraycopy(nodes, index, nodes, index + 1, size - index);
		nodes[index] = element;
		size++;
		if(addListener != null) addListener.onAdd(this, element);
	}
	
	public boolean remove(T element) {
		for (int index = 0; index < size; index++) {
			if(element.equals(nodes[index])) {
				fastRemove(index);
				return true;
			}
		}
		return false;
	}
	
	public T removeAt(int index) {
		T o = nodes[index];
        fastRemove(index);
        return o;
	}

	private void fastRemove(int index) {
		int numMoved = size - index - 1;
		if (numMoved > 0) {
            System.arraycopy(nodes, index+1, nodes, index, numMoved);
        }
		size--;
	}
	
	public void clear() {
		size = 0;
	}
	
	public boolean contains(T element) {
		for (int index = 0; index < size; index++) {
			if(element.equals(nodes[index])) {
				return true;
			}
		}
		return false;
	}
	
	public int indexOf(T lostSheep) {
		for (int index = 0; index < size; index++) {
			if(lostSheep.equals(nodes[index])) {
				return index;
			}
		}
		return -1;
	}

	
	public int size() {
		return size;
	}
	
	public boolean isEmpty() {
		return size == 0;
	}
	
	public T get(int i) {
		if(i >= size) throw new ArrayIndexOutOfBoundsException(i);
		return nodes[i];
	}
	
	public void set(int i, T element) {
		if(i > size) throw new ArrayIndexOutOfBoundsException(i);
		nodes[i] = element;
	}
	
	public void setAll(NodeList<T> list) {
		nodes = list.nodes;
	}
	
	public T getFirst() {
		if(size == 0) throw new ArrayIndexOutOfBoundsException(0);
		return nodes[0];
	}
	
	public T getLast() {
		if(size == 0) throw new ArrayIndexOutOfBoundsException(0);
		return nodes[size - 1];
	}
	
	public T getBeforeLast() {
		if(size <= 1) throw new ArrayIndexOutOfBoundsException(size - 1);
		return nodes[size - 2];
	}

	public Iterator<T> iterator() {		
		return new Iterator<T>() {

			NodeList<T> list = NodeList.this;
			int index = 0;
			
			public boolean hasNext() {
				return index < list.size();
			}

			public T next() {
				if(index >= list.size()) throw new ArrayIndexOutOfBoundsException(index);
				return list.getNodes()[index++];
			}

			public void remove() {
				NodeList.this.removeAt(index);
			}
			
		};
	}

	public void accept(Visitor visitor) throws IOException {
		visitor.visit(this);
	}

	public void acceptChildren(Visitor visitor) throws IOException {
		for(int i = 0; i < size; i++) {
			if(nodes[i] != null) nodes[i].accept(visitor);
		}
	}

	public boolean hasChildren() {
		return size > 0;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean replace(Node oldie, Node kiddo) {
		int index = indexOf((T) oldie);
		if(index == -1) {
			String oldieClassName = oldie == null ? "null" : oldie.getClass().getSimpleName();
			String kiddoClassName = kiddo == null ? "null" : kiddo.getClass().getSimpleName();
			System.out.println("Trying to replace "+oldie+" with "+kiddo+" in a list with \n"+toString());
			throw new ArrayIndexOutOfBoundsException("Trying to replace a "
					+oldieClassName+" with a "+kiddoClassName+
					" in a "+this.getClass().getSimpleName()+", but couldn't find node to replace in NodeList.");
		}
		nodes[index] = (T) kiddo;
		return true;
	}

	public void addAll(NodeList<T> list) {
		int newSize = size + list.size;
		ensureCapacity(newSize);
		System.arraycopy(list.nodes, 0, nodes, size, list.size);
		size = newSize;
	}
	
	public void addAll(List<T> list) {
		int newSize = size + list.size();
		ensureCapacity(newSize);
		int index = size;
		for(T o : list) {
			nodes[index++] = o;
		}
	}

	public T[] getNodes() {
		return nodes;
	}
	
	@Override
	public String toString() {
		return toString(false);
	}
	
	public String toString(boolean stackLike) {
		return toString(stackLike, 0);
	}
	
	public String toString(boolean stackLike, int offset) {
		if(size == 0) return "[]";
		StringBuilder sB = new StringBuilder();
		for(int i = 0; i < offset; i++) sB.append("  ");
		if(stackLike) {
			sB.append('\n');
		} else {
			sB.append('[');
		}
		int index = 0;
		while(index < size) {
			T node = nodes[index++];
			if(node instanceof NodeList<?>) {
				sB.append(((NodeList<?>) node).toString(false, stackLike ? offset + index : offset));
			} else {
				if(stackLike) {
					for(int i = 0; i < index; i++) sB.append("  ");
				} else if(index > 1) {
					sB.append(", ");
				}
				if(node == null) sB.append("null");
				else sB.append(node.toString());
			}
			if(stackLike && index < size) sB.append("\n");
		}
		if(!stackLike) sB.append(']');
		return sB.toString();
	}

	public void push(T node) {
		if(size + 1 > nodes.length) realloc();
		nodes[size++] = node;
	}
	
	/**
	 * Checked pop: ensures it's this node we are removing
	 * @param coverDecl
	 */
	public void pop(T node) {
		if(peek() == node)
			pop();
		else
			throw new Error("Unmatched node in checked pop: "+node+". peek is "+peek());
	}

	public void pop() {
		if(size <= 0) throw new ArrayIndexOutOfBoundsException(0);
		size--;
	}

	public T peek() {
		return nodes[size - 1];
	}
	
	public T peek(int i) {
		return nodes[size - i];
	}
	
	public int find(Class<?> clazz) {
		return find(clazz, size - 1);
	}
		
	public int find(Class<?> clazz, int offset) {
		int i = offset;
		while(i >= 0) {
			T node = nodes[i];
			if(clazz.isInstance(node)) {
				return i;
			}
			i--;
		}
		
		return -1;
	}

	public Module getModule() {
		return (Module) nodes[0];
	}
	
	public void addBefore(T beforeWhat, T kiddo) {
		int index = indexOf(beforeWhat);
		if(index == -1) {
			throw new Error("Trying to add "+kiddo+" before "+beforeWhat+", but it can't be found in the list.");
		}
		add(index, kiddo);
	}

	public void addAfter(T afterWhat, T kiddo) {
		int index = indexOf(afterWhat);
		if(index == -1) {
			throw new Error("Trying to add "+kiddo+" after "+afterWhat+", but it can't be found in the list.");
		}
		add(index + 1, kiddo);
	}

	public boolean addAll(Collection<? extends T> c) {
		for(T t : c) {
			add(t);
		}
		return true;
	}

	public boolean contains(Object o) {
		return contains(o);
	}

	public boolean containsAll(Collection<?> c) {
		boolean result = true;
		for(T t : this) {
			if(!c.contains(t)) {
				result = false;
				break;
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public boolean remove(Object o) {
		return remove((T) o);
	}

	public boolean removeAll(Collection<?> c) {
		boolean removed = false;
		for(T t : this) {
			if(c.contains(t)) {
				removed = true;
				remove(t);
			}
		}
		return removed;
	}

	public boolean retainAll(Collection<?> c) {
		boolean removed = false;
		for(T t : this) {
			if(!c.contains(t)) {
				removed = true;
				remove(t);
			}
		}
		return removed;
	}

	public Object[] toArray() {
		Object[] array = new Object[size];
		System.arraycopy(nodes, 0, array, 0, size);
		return array;
	}

	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] arg) {
		T[] array = (T[]) new Object[size];
		System.arraycopy(nodes, 0, array, 0, size);
		return array;
	}
	
}

