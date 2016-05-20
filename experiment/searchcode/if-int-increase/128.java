package uk.ac.lkl.migen.mockup.handshaker;

import java.util.*;

import uk.ac.lkl.migen.mockup.handshaker.event.*;

public class HandshakeModel {

    private ArrayList<HandshakeListener> listeners;

    /**
     * Indicates whether two people have shaken hands or not.
     * 
     */
    private ArrayList<ArrayList<Integer>> handshakeCounts;

    public HandshakeModel() {
	listeners = new ArrayList<HandshakeListener>();
	handshakeCounts = new ArrayList<ArrayList<Integer>>();
    }

    public void setSize(int size) {
	int currentSize = handshakeCounts.size();

	if (size == currentSize)
	    return;

	if (size < 1)
	    size = 1;

	if (size > currentSize)
	    increaseHandshakeMatrix(currentSize, size);
	else
	    decreaseHandshakeMatrix(currentSize, size);

	fireNumPeopleChanged();
    }

    public int getHandshakeCount(NodePair nodePair) {
	return getHandshakeCount(nodePair.fromIndex, nodePair.toIndex);
    }

    public int getHandshakeCount(int fromIndex, int toIndex) {
	return handshakeCounts.get(fromIndex).get(toIndex);
    }

    private void decreaseHandshakeMatrix(int currentSize, int newSize) {
	// remove rows - make sure send events
	for (int i = currentSize - 1; i >= newSize; i--) {
	    ArrayList<Integer> flagSet = handshakeCounts.get(i);
	    for (int j = 0; j < currentSize; j++) {
		int handshakeCount = flagSet.get(j);
		for (int k = 0; k < handshakeCount; k++)
		    fireHandshakeRemoved(i, j);
	    }
	    handshakeCounts.remove(i);
	}

	// remove columns
	for (int i = 0; i < newSize; i++) {
	    ArrayList<Integer> flagSet = handshakeCounts.get(i);
	    for (int j = currentSize - 1; j >= newSize; j--) {
		int handshakeCount = flagSet.get(j);
		for (int k = 0; k < handshakeCount; k++)
		    fireHandshakeRemoved(i, j);
		flagSet.remove(j);
	    }
	}
    }

    private void increaseHandshakeMatrix(int currentSize, int newSize) {
	// pad out existing flagSets
	for (int i = 0; i < currentSize; i++) {
	    ArrayList<Integer> flagSet = handshakeCounts.get(i);
	    for (int j = currentSize; j < newSize; j++)
		flagSet.add(0);
	}

	// add new flagSets
	for (int i = currentSize; i < newSize; i++) {
	    ArrayList<Integer> flagSet = new ArrayList<Integer>();
	    for (int j = 0; j < newSize; j++)
		flagSet.add(0);
	    handshakeCounts.add(flagSet);
	}
    }

    // only works nicely for single digits
    public void printFlagSets() {
	for (int i = 0; i < handshakeCounts.size(); i++) {
	    ArrayList<Integer> flagSet = handshakeCounts.get(i);
	    for (int j = 0; j < flagSet.size(); j++) {
		int count = flagSet.get(j);
		System.out.print(count);
	    }
	    System.out.println();
	}
    }

    public void setHandshakeCount(NodePair nodePair, int handshakeCount) {
	setHandshakeCount(nodePair.fromIndex, nodePair.toIndex, handshakeCount);
    }

    public void setHandshakeCount(int fromIndex, int toIndex, int handshakeCount) {
	if (handshakeCount < 0)
	    return;

	int currentHandshakeCount = getHandshakeCount(fromIndex, toIndex);

	int increase = handshakeCount - currentHandshakeCount;

	if (increase == 0)
	    return;

	if (increase > 0)
	    for (int i = 0; i < increase; i++)
		addHandshake(fromIndex, toIndex);
	else
	    // hack
	    for (int i = increase; i < 0; i++)
		removeHandshake(fromIndex, toIndex);
    }

    public int getTotalHandshakeCount() {
	int total = 0;
	for (int i = 0; i < handshakeCounts.size(); i++) {
	    for (int j = 0; j < handshakeCounts.size(); j++) {
		total += getHandshakeCount(i, j);
	    }
	}
	return total;
    }

    public void addHandshake(int fromIndex, int toIndex) {
	ArrayList<Integer> flagSet = handshakeCounts.get(fromIndex);
	int currentCount = flagSet.get(toIndex);

	flagSet.set(toIndex, currentCount + 1);
	fireHandshakeAdded(fromIndex, toIndex);
    }

    public void removeHandshake(int fromIndex, int toIndex) {
	ArrayList<Integer> flagSet = handshakeCounts.get(fromIndex);
	int currentCount = flagSet.get(toIndex);

	// do nothing if already have no handshakes
	if (currentCount == 0)
	    return;

	flagSet.set(toIndex, currentCount - 1);
	fireHandshakeRemoved(fromIndex, toIndex);
    }

    public String getPersonLabel(int index) {
	return Character.toString((char) (index + 65));
    }

    public int getSize() {
	return handshakeCounts.size();
    }

    private void fireNumPeopleChanged() {
	HandshakeEvent e = new HandshakeEvent(this);
	for (HandshakeListener listener : listeners)
	    listener.numPeopleChanged(e);
    }

    private void fireHandshakeAdded(int fromIndex, int toIndex) {
	HandshakeEvent e = new HandshakeEvent(this, fromIndex, toIndex);
	for (HandshakeListener listener : listeners)
	    listener.handshakeAdded(e);
    }

    private void fireHandshakeRemoved(int fromIndex, int toIndex) {
	HandshakeEvent e = new HandshakeEvent(this, fromIndex, toIndex);
	for (HandshakeListener listener : listeners)
	    listener.handshakeRemoved(e);
    }

    public void addHandshakeListener(HandshakeListener listener) {
	listeners.add(listener);
    }

    public void removeHandshakeListener(HandshakeListener listener) {
	listeners.remove(listener);
    }

}

