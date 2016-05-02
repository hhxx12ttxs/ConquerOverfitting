/*
 * Author: Carl van der Smissen
 * Date: 2/4/2012
 * Terminal: 6
 * Class: COSC2320
 */
package COSC2320.MYDATASTRUCTURES.LIST;
public class CVAList<T> implements CVListInterface<T> {

    // Member Variables
    private T[] items;
    private int numOfElements;
    private int size;
    private static final int SIZE_LIMIT = 50;

    // Default and Additional Constructors
    public CVAList() {
        items = (T[]) new Object[SIZE_LIMIT];
        numOfElements = 0;
        size = SIZE_LIMIT;
    }

    // @return int numOfElements
    public int getNumOfElements() {
        return numOfElements;
    }

    // add checks first to see if we have reached the capacity of our array
    // if we have it calls increaseSize before proceeding.
    // the newItem that is passed is then added to the array with an offset of 0
    // before numOfElements is increased since it is measured with an offset of 1.
    // @return boolean true upon success.
    public boolean add(T newItem) {
        boolean result = false;
        if (isFull()) {
            if (!increaseSize()) {
                return false;
            }
        }
        items[numOfElements] = newItem;
        numOfElements++;
        result = true;
        return result;
    }

    // remove will first check if the array isEmpty , if it does indeed contain
    // elements then it will proceed.
    public boolean remove(int numOfItem) {
        if (!isEmpty()) {
            // If the numOfItem is equal to the numOfElements
            // then it is the last one in the list and we simply null it out.
            if (numOfElements == numOfItem) {
                items[numOfItem] = null;
                numOfElements--;
                return true;
            } // If the numOfElements are greater than the numOfItems then we need
            // to move all the items that came after the removed object up one space.
            else if (numOfElements > numOfItem) {
                int i = numOfItem;
                do {
                    items[i] = items[i + 1];
                    i++;
                } while (i <= numOfElements);
                numOfElements--;
                return true;
            }
            return false;
        }
        // If it turns out it is empty then make sure the numOfElements is 0.
        numOfElements = 0;
        return false;
    }

    // removeAll will just create a new array at the default size limit and reset
    // the numOfElements back to 0.
    public boolean removeAll() {
        items = (T[]) new Object[SIZE_LIMIT];
        numOfElements = 0;
        size = SIZE_LIMIT;
        return true;
    }

    // search will take the object passed and run through if the array isn't empty
    // to see if the exact item is found. It uses the items equals method for
    // the comparison. If not overridden then the items must be the same item to match.
    // @return int -1 upon failure to locate.
    public int search(T item) {
        if (!isEmpty()) {
            for (int i = 0; i < numOfElements; i++) {
                if (item.equals(items[i])) {
                    return i;
                }
            }
            return -1;
        }
        return -1;
    }

    // move will take an object from one position in the array to another if the
    // array is not empty.
    public boolean move(int oldPos, int newPos) {
        if (!isEmpty()) {
            if (items[oldPos] != null) {
                T oldItem = items[oldPos];
                // If the oldPos is greater than the newPos then we have to move
                // all of the items up on position in the array to make space for
                // adding the new item.
                if (oldPos > newPos) {
                    for (int i = oldPos; i > newPos; i--) {
                        items[i] = items[i - 1];
                    }
                    items[newPos] = oldItem;
                    return true;
                } // If the oldPos is less than the newPos and not greater than the
                // numOfElements (change it if it is to high), we need to move all
                // items down one position before inserting the object at newPos.
                else if (oldPos < newPos) {
                    if (newPos > numOfElements - 1) {
                        newPos = numOfElements - 1;
                    }
                    if (newPos < numOfElements) {
                        for (int i = oldPos; i < newPos; i++) {
                            items[i] = items[i + 1];
                        }
                        items[newPos] = oldItem;
                        return true;
                    }
                } // If the oldPos is equal to the newPos then nothing has to be moved.
                else if (oldPos == newPos) {
                    return true;
                }
            }
        }
        return false;
    }

    // retrieve is used to return the object located at a specific index.
    // If empty or out of bounds, it returns null.
    public T retrieve(int itemNum) {
        if (!isEmpty()) {
            if (itemNum <= numOfElements) {
                return items[itemNum];
            }
        }
        return null;
    }

    // if the numOfElements is equal to the size of the array then it is full.
    public boolean isFull() {
        if (numOfElements == size) {
            return true;
        }
        return false;
    }

    // if there are no elements in the array it is empty.
    public boolean isEmpty() {
        if (numOfElements == 0) {
            return true;
        }
        return false;
    }

    // function used to double the current array size and copy over all elements.
    // if out of memory it will return false.
    public boolean increaseSize() {
        try {
            size *= 2;
            T[] temp = (T[]) new Object[size];
            System.arraycopy(items, 0, temp, 0, numOfElements);
            T[] oldArray = items;
            items = temp;
            return true;
        } catch (OutOfMemoryError E) {
            return false;
        }

    }
    // creates a large string of all the toStrings of each item in the array.

    @Override
    public String toString() {
        String output = "";
        if (!isEmpty()) {
            for (int i = 0; i < numOfElements; i++) {
                output += items[i].toString() + "\n";
            }
        }
        return output;
    }
}

