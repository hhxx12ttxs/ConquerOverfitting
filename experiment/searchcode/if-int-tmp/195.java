package com.leetcode;

import java.util.Arrays;

public class MergeSortedArray {
	public static void main(String[] args) {
		int[] A = {1};
		int[] B = {2};
		merge2(A,2,B,1);
		//
		for (int i = 0; i < A.length; i++) {
			System.out.print(A[i]+" ");
		}
	}

	public static void merge(int A[], int m, int B[], int n) {
		if(A==null || B==null){
			return;
		}
		if(m+n>A.length){
			return;
		}
		int[] tmp = new int[m+n];
		for (int i = 0; i < m; i++) {
			tmp[i] = A[i];
		}
		for (int i = 0; i < n; i++) {
			tmp[m+i] = B[i];
		}
		Arrays.sort(tmp);
		for (int i = 0; i < tmp.length; i++) {
			A[i] = tmp[i];
		}
	}
	public static void merge2(int A[], int m, int B[], int n) {
		if(n<1){
			return;
		}
		int k = m+n-1;
		int i = m-1;
		int j = n-1;
		while(i>=0 && j>=0){
			A[k--] = A[i]>B[j]?A[i--]:B[j--];
		}
//		while(j>=0){
//			A[k--] = B[j--];
//		}
	}
	
}

