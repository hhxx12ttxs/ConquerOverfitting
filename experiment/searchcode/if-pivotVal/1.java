/*
 * This file is part of seadams Utils.
 *
 * Copyright (c) 2008-2011 Sam Adams <seadams@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.seadams.util.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeysValuesSorter {

    /**
     * <p>Sorts key and value list by contents of value. Uses quick sort algorithm.
     *
     * <p>http://linux.wku.edu/~lamonml/algor/sort/quick.html
     *
     * @param <K>
     * @param <V>
     * @param keyList
     * @param valueList
     */
    public static <K, V extends Comparable<? super V>> void quickSortByValues(List<K> keyList, List<V> valueList) {
        if (keyList.size() != valueList.size()) {
            System.err.println("List size mis-match");
            System.exit(-1);
        }

        quickSortByValues(keyList, valueList, 0, keyList.size() - 1);
    }

    protected static <K, V extends Comparable<? super V>> void quickSortByValues(List<K> keyList, List<V> valueList, int left, int right) {

        if (keyList.size() < 2) {
            return;
        }

        List<Integer> leftList = new ArrayList<Integer>();
        List<Integer> rightList = new ArrayList<Integer>();
        leftList.add(left);
        rightList.add(right);

        while (leftList.size() > 0) {
            left = leftList.remove(leftList.size()-1);
            right = rightList.remove(rightList.size()-1);

            // Store values of left and right
            int leftStored = left;
            int rightStored = right;

            // Store pivot values
            int pivot = left;
            V pivotVal = valueList.get(left);
            K pivotKey = keyList.get(left);

            while (left < right) {

                while ((((Comparable<? super V>)valueList.get(right)).compareTo(pivotVal) >= 0) && (left < right)) {
                    right --;
                }
                if (left != right) {
                    valueList.set(left, valueList.get(right));
                    keyList.set(left, keyList.get(right));
                    left ++;
                }

                while ((((Comparable<? super V>)valueList.get(left)).compareTo(pivotVal) <= 0) && (left < right)) {
                    left ++;
                }
                if (left != right) {
                    valueList.set(right, valueList.get(left));
                    keyList.set(right, keyList.get(left));
                    right --;
                }
            }

            valueList.set(left, pivotVal);
            keyList.set(left, pivotKey);

            pivot = left;
            left = leftStored;
            right = rightStored;

            if (right > pivot) {
                leftList.add(pivot + 1);
                rightList.add(right);
            }
            if (left < pivot) {
                leftList.add(left);
                rightList.add(pivot - 1);
            }
        }
    }


    public static <K> List<K> quickSortByValues(Map<K, ? extends Comparable<?>>... valueMaps) {
        int nVals = valueMaps.length;
        if (nVals == 0) {
            throw new IllegalArgumentException("No maps");
        }

        // Generate key list from first map
        int size = valueMaps[0].size();
        List<K> keys = new ArrayList<K>(size);
        keys.addAll(valueMaps[0].keySet());

        // Check other maps
        for (int i = 1; i < nVals; i ++) {
            if (valueMaps[i].size() != size) {
                throw new IllegalArgumentException("Map size mis-match");
            }
            if (!valueMaps[i].keySet().containsAll(keys)) {
                throw new IllegalArgumentException("Map key mis-match");
            }
        }

        quickSortByValues(keys, 0, size-1, valueMaps);

        return keys;
    }

    protected static <K> void quickSortByValues(List<K> keyList, int left, int right, Map<K, ? extends Comparable<?>>... valueMaps) {

        if (keyList.size() < 2) {
            return;
        }

        List<Integer> leftList = new ArrayList<Integer>();
        List<Integer> rightList = new ArrayList<Integer>();
        leftList.add(left);
        rightList.add(right);

        while (leftList.size() > 0) {
            left = leftList.remove(leftList.size()-1);
            right = rightList.remove(rightList.size()-1);

            // Store values of left and right
            int leftStored = left;
            int rightStored = right;

            // Store pivot values
            int pivot = left;
            K pivotKey = keyList.get(left);

            while (left < right) {

                while (compare(pivotKey, keyList, right, valueMaps) >= 0 && (left < right)) {
                    right --;
                }
                if (left != right) {
                    keyList.set(left, keyList.get(right));
                    left ++;
                }

                while (compare(pivotKey, keyList, left, valueMaps) <= 0 && (left < right)) {
                    left ++;
                }
                if (left != right) {
                    keyList.set(right, keyList.get(left));
                    right --;
                }
            }

            keyList.set(left, pivotKey);

            pivot = left;
            left = leftStored;
            right = rightStored;

            if (right > pivot) {
                leftList.add(pivot + 1);
                rightList.add(right);
            }
            if (left < pivot) {
                leftList.add(left);
                rightList.add(pivot - 1);
            }
        }
    }

    @SuppressWarnings("unchecked")
	private static <K> int compare(K pivotKey, List<K> keyList, int index, Map<K, ? extends Comparable<?>>... valueMaps) {
        int result = 0;
        int i = 0;
        int n = valueMaps.length;
        K compKey = keyList.get(index);
        while (result == 0 && i < n) {
            Comparable pivotVal = valueMaps[i].get(pivotKey);
            Comparable compVal = valueMaps[i].get(compKey);
            result = compVal.compareTo(pivotVal);
            i ++;
        }
        return result;
    }



    public static void main(String[] args) {
        test1();

        test2();

    }

    private static void test1() {
        List<Integer> keys = Arrays.asList(
                new Integer[] {
                        7, 4, 3, 5, 2, 6, 1
                }
                );


        List<Integer> v1 = Arrays.asList(
                new Integer[] {
                        73, 12, 10, 18, 6, 55, 2
                }
                );

        quickSortByValues(keys, v1);
        System.out.println(keys);
    }

    private static void test2() {
//        List<Integer> keys = Arrays.asList(
//                new Integer[] {
//                        7, 4, 3, 5, 2, 6, 1
//                }
//                );

        Map<Integer, Integer> m1 = new HashMap<Integer, Integer>();
        m1.put(7, 73);
        m1.put(14, 10);
        m1.put(32, 10);
        m1.put(7, 10);
        m1.put(2, 6);
        m1.put(0, 55);
        m1.put(1, 2);

        Map<Integer, Integer> m2 = new HashMap<Integer, Integer>();
        m2.put(7, 7);
        m2.put(14, 2);
        m2.put(32, 1);
        m2.put(7, 3);
        m2.put(2, 12);
        m2.put(0, 8);
        m2.put(1, 91);


        for (int i : quickSortByValues(m1, m2)) {
            System.out.println(m1.get(i) + "\t" + m2.get(i));
        }
    }







}

