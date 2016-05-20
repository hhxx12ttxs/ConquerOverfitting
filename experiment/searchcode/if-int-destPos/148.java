package main.mesh;

import java.util.Arrays;

/**
 * @author David Gronlund
 */
public class IntList {

    private int[] data;
    private int size = 0;

    public IntList() {
        data = new int[16];
    }

    public IntList(int length) {
        data = new int[length];
    }

    public IntList(int length, int init) {
        size = length;
        data = new int[length];
        Arrays.fill(data, init);
    }

    public IntList(int[] array) {
        size = array.length;
        data = new int[array.length];
        System.arraycopy(array, 0, data, 0, array.length);
    }

    public int get(int i) {
        return data[i];
    }

    public void add(int f) {
        if (size >= data.length) {
            int[] temp = new int[data.length * 2];
            System.arraycopy(data, 0, temp, 0, data.length);
            data = temp;
        }
        data[size] = f;
        size++;
    }

    public void set(int i, int f) {
        if (size < i + 1) {
            size = i + 1;
        }
        data[i] = f;
    }

    public int size() {
        return size;
    }

    public int getCapacity() {
        return data.length;
    }

    public void ensureCapacity(int capacity) {
        while (data.length < capacity) {
            int[] temp = new int[data.length * 2];
            System.arraycopy(data, 0, temp, 0, data.length);
            data = temp;
        }
    }

    public int[] getArray() {
        int[] temp = new int[size];
        System.arraycopy(data, 0, temp, 0, size);
        return temp;
    }

    public int[] getRawArray() {
        return data;
    }

    public void copy(int destPos, int[] src, int srcPos, int length) {
        if (size < destPos + length) {
            size = destPos + length;
            ensureCapacity(size);
        }
        for (int i = 0; i < length; i++) {
            data[destPos + i] = src[srcPos + i];
        }
    }

    public boolean sectionsEqual(int destPos, int[] src, int srcPos, int length) {
        for (int i = 0; i < length; i++) {
            if (data[destPos + i] != src[srcPos + i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return Arrays.toString(data);
    }
}

