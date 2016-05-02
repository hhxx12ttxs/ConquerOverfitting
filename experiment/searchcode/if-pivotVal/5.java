/* === This file is part of Jive JML dynamic program verifier ===
 *
 *   Copyright 2012, Arvind S Raj <sraj[dot]arvind[at]gmail[dot]com>
 *
 *   Jive JML dynamic program verifier is free software: you can redistribute it
 *   and/or modify it under the terms of the GNU General Public License as
 *   published by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   Jive JML Dynamic Program Verifier is distributed in the hope that it
 *   will be useful, but WITHOUT ANY WARRANTY; without even the implied
 *   warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *   See the GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Jive JML Dynamic Program Verifier. If not, see
 *   <http://www.gnu.org/licenses/>.
 */

package quicksort;

import java.io.*;

public class QuickSort {
    private /* @spec_public@ */ int numbers [];

    public QuickSort (int size)
    {
        if (size < 1) size = 1;
        numbers = new int[size];
        for (int i = 0; i < size ; i ++)
        {
            numbers[i] = (int)(Math.random() * 100);
        }
    }

    public void displayNumbers() {
        for(int counter = 0; counter < numbers.length; counter++) {
            System.out.println(numbers[counter]);
        }
        return;
    }

    /*@ public normal_behavior
      @ requires numbers != null && numbers.length > 0;
      @ ensures \old(numbers.length) == numbers.length ;
      @ ensures(\forall int i; 0 <= i && i < numbers.length; numbers[i] <= numbers[i +1]);
      @ */
    public void quickSort ()
    {
        quickSort (0, numbers.length - 1);
    }

    private /* @spec_public@ */ void quickSort(int low , int high)
    {
        if (low < high)
        {
            int middle = partition (low, high);
            quickSort (low, middle);
            quickSort (middle + 1, high);
        }
    }

    private /* @spec_public@ */ int partition (int low , int high)
    {
        int pivotVal, temp, storeIndex;
        pivotVal = numbers[low];
        storeIndex = low;
        numbers[low] = numbers[high];
        numbers[high] = pivotVal;
        for(int counter = low; counter < high; counter++) {
            if(numbers[counter] < pivotVal) {
                temp = numbers[counter];
                numbers[counter] = numbers[storeIndex];
                numbers[storeIndex] = temp;
                storeIndex = storeIndex + 1;
            }
        }
        numbers[high] = numbers[storeIndex];
        numbers[storeIndex] = pivotVal;
        return storeIndex;
    }
}

