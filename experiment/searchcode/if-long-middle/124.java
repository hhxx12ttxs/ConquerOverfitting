package com.algorithm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: ryang
 * Date: 2013-07-02
 * Time: 11:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class InversionCounting {

    public static void main( String[] args ) throws FileNotFoundException, IOException
    {
        if(args.length < 1) {
            System.out.println("need provide path of integer array");
            System.exit(-1);
        }

        int[] ia = new int[100000];
        int index = 0;

        BufferedReader br = new BufferedReader(new FileReader(args[0]));
        try {

            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                int number = Integer.parseInt(line);
                ia[index++] = number;
                line = br.readLine();
            }

        } finally {
            br.close();
        }


        int[] test1 = {1,3,2,0,6,4};
        System.out.println(count(ia,0, ia.length-1));
    }


    public static long countSplit(int[] arr, int low, int middle, int high) {

        long numOfInversion = 0;
        int lenLeft = middle - low + 1;
        int lenRight = high - middle;

        int[] left = new int[lenLeft ];
        int[] right = new int[lenRight];

        for(int i= 0 ; i< lenLeft; i ++) {
            left[i] = arr[low + i];
        }

        for(int j = 0; j< lenRight; j++) {
            right[j] =  arr[middle + 1 + j];
        }

        int i=0, j=0, k= low;
        while(i < lenLeft && j < lenRight) {
            if(left[i] <= right[j]) {
                arr[k++] = left[i++];
            } else {
                arr[k++] = right[j++];
                numOfInversion += lenLeft - i;
            }
        }
        while(i < lenLeft) {
            arr[k++] = left[i++];
        }

        while(j < lenRight) {
            arr[k++] = right[j++];
        }

        return numOfInversion;

    }

    public static long count(int[] arr, int low, int high) {
        if(low < high) {
            int middle = low + (high - low) / 2;
            long left = count(arr, low, middle);
            long right = count(arr, middle+1, high);
            long split = countSplit(arr, low, middle, high);
            return left + right + split;
        } else
            return 0;
    }
}

