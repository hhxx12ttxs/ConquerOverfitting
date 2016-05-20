package main.mesh;

import java.util.Arrays;

/**
 * @author David Gronlund
 */
public class FloatList {

    private float[] data;
    private int size = 0;

    public FloatList() {
        data = new float[16];
    }

    public FloatList(int length) {
        data = new float[length];
    }

    public FloatList(int length, float init) {
        size = length;
        data = new float[length];
        Arrays.fill(data, init);
    }

    public FloatList(float[] array) {
        size = array.length;
        data = new float[array.length];
        System.arraycopy(array, 0, data, 0, array.length);
    }

    public float get(int i) {
        return data[i];
    }

    public void add(float f) {
        if (size >= data.length) {
            float[] temp = new float[data.length * 2];
            System.arraycopy(data, 0, temp, 0, data.length);
            data = temp;
        }
        data[size] = f;
        size++;
    }

    public void set(int i, float f) {
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
        while (data.length <= capacity) {
            float[] temp = new float[data.length * 2];
            System.arraycopy(data, 0, temp, 0, data.length);
            data = temp;
        }
    }

    public float[] getArray() {
        float[] temp = new float[size];
        System.arraycopy(data, 0, temp, 0, size);
        return temp;
    }

    public float[] getRawArray() {
        return data;
    }

    public void copy(int destPos, float[] src, int srcPos, int length) {
        ensureCapacity(destPos + length);
        if (size < destPos + length) {
            size = destPos + length;
        }
        for (int i = 0; i < length; i++) {
            data[destPos + i] = src[srcPos + i];
        }
    }

    public boolean sectionsEqual(int destPos, float[] src, int srcPos, int length) {
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

