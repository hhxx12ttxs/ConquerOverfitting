package com.feby.hackerrank.tkpd.group;

import java.util.Scanner;

public class Solution {
	
	private static final int RIGHT = 1;
	private static final int DOWN = 1;
	
	private static int fieldCount = 0;
	private static int R;
	private static int C;
	private static char[][] fields;

	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		
		R = in.nextInt();
		C = in.nextInt();
		
		fields = new char[R][C];
		
		for (int i = 0; i < R; i++) {
			String input = in.next();
			for (int j = 0; j < input.length(); j++) {
				fields[i][j] = input.charAt(j);
			}
		}
		
		calculateFields(0, 0, 'N');
		int[] realFields = new int[fieldCount];
		int solution = 0;

		for (int i = 0; i < factorial(fieldCount); i++) {
			for (int j = 0; j < fieldCount; j++) {
				realFields[j] = i % 2;
			}

			int sheep = 0;
			for (int k = 0; k < fieldCount; k++) {
				if (1 == realFields[k]) {
					sheep++;
				}
			}
			
			if (0 == sheep % 2) {
				solution++;
			}
		}
		
		System.out.println(solution);
		
	}
	
	private static void calculateFields(int row, int column, int trail) {
		if (R == row || C == column) {
			return;
		}
		
		if ('Y' == fields[row][column]) {
			int neighbor = exposeNeighbor(row, column);
			if ('N' == trail && (-1 == neighbor || 'Y' == neighbor)) {
				fieldCount++;
				fields[row][column] = (char) fieldCount;
			} else if ('N' == trail && -1 != neighbor) {
				fields[row][column] = (char) neighbor;
			}
			
			calculateFields(row + DOWN, column, fieldCount);
			calculateFields(row, column + RIGHT, fieldCount);
		} else if ('N' == fields[row][column]) {
			calculateFields(row, column + RIGHT, 'N');
			calculateFields(row + DOWN, column, 'N');
		}
	}
	
	private static int exposeNeighbor(int row, int column) {
		int neighbor = -1;
		
		if (0 <= column - 1 && 'N' != fields[row][column - 1]) {
			neighbor = fields[row][column - 1];
		}
		if (0 <= row - 1 && 'N' != fields[row - 1][column]) {
			neighbor = fields[row - 1][column];
		}
		if (C > column + 1 && 'N' != fields[row][column + 1]) {
			neighbor = fields[row][column + 1];
		}
		if (R > row + 1 && 'N' != fields[row +1][column]) {
			neighbor = fields[row +1][column];
		}
		
		return neighbor;
	}
	
	private static int factorial(int fields) {
		if (1 == fields) {
			return 1;
		}
		
		return fields * factorial(fields - 1);
	}

}

