//Write a program that accepts a base ten (non-fractional) number at the command line
//and outputs the binary representation of that number.
//http://www.cprogramming.com/challenges/dectobin.html
//
// I only wrote the class to handle the binary to decimal conversion rather than the
// command line input. This class will also do 2s compliment on negative integers.
//
// I also know about Integer.toBinaryString(), but I wanted to implement it myself for
// practice.

package com.lucaspate.decimaltobinary;

public class DecimalToBinary {
	String binary;
	int inputNum;
	int binaryArray[];
	static final int MAX_BITS = 32;
	boolean negative;
	
	public DecimalToBinary() {
		this(0);
	}
	
	public DecimalToBinary(int inputNum) {
		binaryArray = new int[MAX_BITS];
		setInputNum(inputNum);
	}
	
	private void decimalToBinary() {
		convertToBinary();
		
		if (negative)
			twosCompliment();
		
		for (int i = MAX_BITS - 1; i >= 0; i--)
			binary = binary + String.valueOf(binaryArray[i]);
		
		if (binary.indexOf('1') >= 0)
			binary = binary.substring(binary.indexOf('1'), binary.length());
		else
			binary = "0";
	}
	
	private void convertToBinary() {
		double powerTwo = 0;
		int maxPower = 0;
		int n = inputNum;
		
		while (n > Math.pow(2, maxPower))
			maxPower++;
		
		if (maxPower > 0)
			maxPower--;
		
		for (int i = maxPower; i >= 0; i--)
		{
			powerTwo = Math.pow(2, i);
			if (n >= powerTwo)
			{
				binaryArray[i] = 1;
				n = n - (int)(powerTwo);
			}
			else
				binaryArray[i] = 0;
		}
	}
	
	private void twosCompliment() {
		boolean carry = false;
		
		// flip all bits
		for (int i = 0; i < MAX_BITS; i++) {
			if (binaryArray[i] == 0) {
				binaryArray[i] = 1;
			}
			else {
				binaryArray[i] = 0;
			}
		}
		
		// add 1
		if (binaryArray[0] == 1) {
			binaryArray[0] = 0;
			carry = true;
		}
		else {
			binaryArray[0] = 1;
		}
		for (int i = 1; i < MAX_BITS - 1; i++) {
			if (carry) {
				if (binaryArray[i] == 0) {
					carry = false;
					binaryArray[i] = 1;
				}
				else {
					carry = true;
					binaryArray[i] = 0;
				}
			}
		}
	}

	public String getBinary() {
		return binary;
	}

	public int getInputNum() {
		if (negative) {
			return -inputNum;
		}
		
		return inputNum;
	}

	public void setInputNum(int inputNum) {
		negative = false;
		if (inputNum < 0) {
			negative = true;
			inputNum = -inputNum;
		}

		for (int i = 0; i < MAX_BITS; i++)
			binaryArray[i] = 0;
		
		this.inputNum = inputNum;
		binary = "";
		
		decimalToBinary();
	}
}

