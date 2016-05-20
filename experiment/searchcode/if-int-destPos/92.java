package main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author David Gronlund
 */
public class Utils {

    public static String loadFile(String url) {
        StringBuilder builder = new StringBuilder();
        BufferedReader file = null;
        try {
            file = new BufferedReader(new FileReader(url));
        } catch (FileNotFoundException ex) {
            System.out.println(ex);
            return null;
        }
        try {
            String temp;
            while ((temp = file.readLine()) != null) {
                if (temp.length() > 0) {
                    builder.append(temp).append("\n");
                }
            }
        } catch (IOException ex) {
            System.out.println(ex);
            return null;
        }
        return builder.toString();
    }

    public static void copyVector4(float[] dest, int destPos, float[] src, int srcPos) {
        for (int i = 0; i < 4; i++) {
            dest[destPos + i] = src[srcPos + i];
        }
    }

    public static float[] getVector4(float[] src, int start) {
        float[] vec = new float[4];
        for (int i = 0; i < 4; i++) {
            vec[i] = src[start + i];
        }
        return vec;
    }

    public static float[] setArraySize(float[] array, int size) {
        float[] temp = new float[size];
        System.arraycopy(array, 0, temp, 0, array.length);
        return temp;
    }

    public static float[] increaseArraySize(float[] array, int amount) {
        float[] temp = new float[array.length + amount];
        System.arraycopy(array, 0, temp, 0, array.length);
        return temp;
    }

    public static int[] setArraySize(int[] array, int size) {
        int[] temp = new int[size];
        System.arraycopy(array, 0, temp, 0, array.length);
        return temp;
    }

    public static int[] increaseArraySize(int[] array, int amount) {
        int[] temp = new int[array.length + amount];
        System.arraycopy(array, 0, temp, 0, array.length);
        return temp;
    }

    public static void printVectors(float[] vectors) {
        if (vectors.length % 4 == 0) {
            for (int i = 0; i < vectors.length; i += 4) {
                System.out.println(vectors[i] + ", " + vectors[i + 1] + ", " + vectors[i + 2] + ", " + vectors[i + 3]);
            }
        }
    }
}

