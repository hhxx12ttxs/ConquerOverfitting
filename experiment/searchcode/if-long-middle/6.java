package com.algorithm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: ryang
 * Date: 2013-07-16
 * Time: 8:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class QuickSorting {

    private static Long comparisonCount = 0L;

    public static void main(String[] args) throws IOException{
        if(args.length < 1) {
            System.out.println("please provide data file");
            System.exit(-1);

        }

        int[] ia = new int[10000];
        int index = 0;

        BufferedReader br = new BufferedReader(new FileReader(args[0]));
        try {
            String line = br.readLine();

            while (line != null) {
                int number = Integer.parseInt(line);
                ia[index++] = number;
                line = br.readLine();
            }

        } finally {
            br.close();
        }

        int[] testArray1 = {4,8,7,1,3,5,6,2,11,16, -1, -5};

        //doSort(testArray1, 0, testArray1.length-1);
        Long min = Long.MAX_VALUE;
        for(int i = 0; i< 1500; i++) {
            comparisonCount = 0L;
            int[] arrClone = (int[])(ia.clone());
            doSort(arrClone,0,arrClone.length-1);
            if(comparisonCount < min) {
                min = comparisonCount;
            }
        }
        System.out.println(min);
    }

    public static void doSort(int[] arr, int low, int high) {
        if(low < high) {
            comparisonCount += high - low;
            //int p = partitionByFirstElement(arr, low, high);
            //int p = partiionByLastElement(arr,low, high);
            //int p = partitionByMedian(arr,low, high);
            int p = partionByRandomElment(arr, low, high);
            doSort(arr,low, p-1);
            doSort(arr,p+1, high);
        }

    }

    public static int partitionByFirstElement(int[] array, int low, int high) {
        int x = array[low];
        int p = low + 1;
        for(int i=low + 1; i<= high; i++) {
            if(array[i] < x) {
                swap(array, p , i);
                p = p + 1;

            }
        }
        swap(array, p-1, low);
        return p-1;
    }

    public static int partiionByLastElement(int[] array, int low, int high) {
       swap(array,low,high);
       return partitionByFirstElement(array, low, high);
    }

    public static int partionByRandomElment(int[] array , int low, int high) {
        Random generator = new Random();
        int p = low + generator.nextInt(high-low + 1);
        swap(array, low, p);
        return partitionByFirstElement(array, low, high);
    }

    public static int partitionByMedian(int[] array, int low, int high) {
        int length = high - low + 1;
        int middle = length % 2 == 0 ? low + length/2 -1 : low + length / 2;

        if((array[low] - array[middle]) * (array[high] - array[low]) > 0)
           return partitionByFirstElement(array,low, high);
        else if( (array[middle] - array[low]) * (array[high] - array[middle]) > 0) {
            swap(array,low, middle);
        } else {
            swap(array, low, high);
        }
        return  partitionByFirstElement(array,low, high);

    }

    public static int median(int a, int b, int c) {
        if ( (a - b) * (c - a) >= 0 ) // a >= b and a <= c OR a <= b and a >= c
            return a;
        else if ( (b - a) * (c - b) >= 0 ) // b >= a and b <= c OR b <= a and b >= c
            return b;
        else
            return c;
    }


    public static void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
}

