package uk.ac.lkl.common.util.collections;

import java.util.Collection;

/**
 * Wraps up a list so that can see additions/removals.
 * 
 * @author $Author: darren.pearce $
 * @version $Revision: 4642 $
 * @version $Date: 2010-03-10 23:23:25 +0100 (Wed, 10 Mar 2010) $
 * 
 */
public class NotifyingList<E> extends AbstractNotifyingList<E> {

    public NotifyingList(Class<E> elementClass) {
	super(elementClass);
    }

    public NotifyingList(Class<E> elementClass, E[] elements) {
	super(elementClass, elements);
    }

    public NotifyingList(Class<E> elementClass, Collection<E> collection) {
	super(elementClass, collection);
    }

    @Override
    public boolean add(E element) {
	int index = size();
	super.add(element);
	fireElementAdded(element, index);
	return true;
    }

    @Override
    public void add(int index, E element) {
	super.add(index, element);
	fireElementAdded(element, index);

	for (int i = index; i < size() - 1; i++) {
	    E movedElement = get(i);
	    fireElementMoved(movedElement, i, i + 1);
	}
    }

    public boolean removeElement(E element) {
	int index = indexOf(element);

	if (index == -1)
	    return false;

	remove(index);
	return true;
    }

    public E remove(int index) {
	E result = super.remove(index);
	fireElementRemoved(result, index);

	for (int i = index; i < size(); i++) {
	    E movedElement = get(i);
	    fireElementMoved(movedElement, i + 1, i);
	}
	return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object element) {
	// calls remove element
	return removeElement((E) element);
    }

}

