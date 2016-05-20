package fiveBanks;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;

import Jama.Matrix;


public class Model2 {
	
	static ArrayList<Bank> allBanks = new ArrayList<Bank>();
	 //stores whether bank failed or not
	
	//-------------------------------------------------------------------------------------------------------//
	
	
	static double[][] globalMatrix1 = new double[5][5]; //the LHS of the eqn
	static double[][] globalMatrix2 = new double[5][1]; //RHS
	
	static double[][] bArr1;
	static double[][] bArr2;
	static double[][] bArr3;
	static double[][] bArr4;
	static double[][] bArr5;
	
	static double [][] debtArr1 = { {1}, {3, 10}, {4, 1} };
	static double [][] debtArr2 = { {2}, {1, 6}, {4, 4} };
	static double [][] debtArr3 = { {3}, {2, 4} };
	static double [][] debtArr4 = { {4}, {5, 13} };
	static double [][] debtArr5 = { {5}, {3, 8} };
	
	static double [][][] allConnections = new double[5][5][5];
	static double [][][] allDebts = new double[5][5][5];
	
	static HashMap<Double, Double> solvencyMap = new HashMap<Double, Double>();
	static HashMap<Double, Boolean> bankFailure = new HashMap<Double, Boolean>();
	
	static final Integer numberOfBanks = 5; 
	static int globalCount = 1; //for debug purposes
	
	
	
	
	//-------------------------------------------------------------------------------//
	
	static double[] allPayments = new double[5]; //keeps track of all payments
	
	
	static double[] baseAmounts = new double[5]; //keeps track of base amounts
	
	static ArrayList<ArrayList<Integer>> bankCreditors = new ArrayList<ArrayList<Integer>>(); 
	//an arrayList for storing the creditors for each bank. outer A_L: index starts at 0, inner : at 1 
	
	
	static boolean [] matrixArray = new boolean[3];
	

	
	static int failureCount = 0;
	
	
	public static void BankSetup(){ //the scope of A,B and C is wrong!!!! Also, we have Matrix A, B. Be careful!
		
		Bank A = new Bank("A", 7.);
		Bank B = new Bank("B", 9.);
		Bank C = new Bank("C", 4.);
		
		A.addDebtor(B, 12.); //debtor owes you money
		A.addCreditor(C, 10.);
		
		B.addDebtor(C, 7.);
		B.addCreditor(A, 12.);
		
		C.addDebtor(A, 10.);
		C.addCreditor(B, 7.);
	
	}
	
	//Takes a String array and an index and then returns the corresponding subMatrix
	
	//type: whether it belongs in the const or linear regime. const = true
	//row: what row of the global Matrix will this belong to?
	
	//NOTE: AT THIS POINT IN TIME, WE ARE STORING ALL THE VARIABLES IN A FIXED SIZE ARRAY. THERE CAN ONLY BE 
	//3 VARIABLES BECAUSE WE ARE DEALING WITH 3 BANKS. AS WE INCREASE THE NUMBER OF BANKS, THIS PART WILL HAVE
	// TO BE MODIFIED ACCORDINGLY
	
	public static void MatrixSetup(int bankNum, double [][] arr, int row){
		
		//allPayments[row] = arr[0]; // this saves the payment value for later comparison
		//baseAmounts[row] = arr[1]; //saves base amount per bank
		
		double num = 0 ; //this will hold either the const value or base value
		
		int count = 0;
		for (double[] x : arr){
			double bankID = x[0];
			double bankBool;
			if (count>0){ 
				bankBool = x[3];
				solvencyMap.put(bankID, bankBool);
			}
			count++;
		}
		
		double bank1 = 0;
		double bank2 = 0;
		double bank3 = 0;
		double bank4 = 0;
		double bank5 = 0;
		
		double min1 = 0;
		double min2 = 0;
		double min3 = 0;
		double min4 = 0;
		double min5 = 0;
		
		ArrayList<Double> allDebtors = new ArrayList<Double>();
		
		int destBank = (int)arr[0][0];
		
		switch (destBank){
		
		case 1:
			bank1 = 1;
			break;
			
		case 2:
			bank2 = 1;
			break;
			
		case 3:
			bank3 = 1;
			break;
			
		case 4:
			bank4 = 1;
			break;
			
		case 5:
			bank5 = 1;
			break;
			
		}
		
		double total = 0; //this will calc RHS
		
		
		for (int c = 1; c < arr.length; c++){
			
			int sourceBank = (int)arr[c][0];
			double min = arr[c][1];
			double base = arr[c][2];
			double type = arr[c][3];
			
			double [][] debtArray = allDebts[sourceBank-1]; 
			
			double totalPay = 0;
			double thisPay = 0;
			
			for (int p = 1; p < debtArray.length; p++){
				
				totalPay = totalPay + debtArray[p][1];
				
				if (debtArray[p][0] == bankNum){
					thisPay = debtArray[p][1];
				}
				
			}
			
			double ratio = thisPay/totalPay;
			
			//ratio = 1;
			
			if (type == 0){ //insolvent
				double temp = base*ratio;
				total += temp;
				
				//total += base;
				//total = total*ratio;
				
				//if (row == 3) System.out.println(total);
							
				switch (sourceBank){
				
				case 1:
					bank1 = -1*ratio;
					min1 = min; 
					break;
					
				case 2:
					bank2 = -1*ratio;
					min2 = min;
					break;
					
				case 3:
					bank3 = -1*ratio;
					min3 = min;
					break;
					
				case 4:
					bank4 = -1*ratio;
					min4 = min;
					break;
					
				case 5:
					bank5 = -1*ratio;
					min5 = min;
					break;
				
				}
				
				
			}
			
			else if (type == 1) { //solvent
				total += min;
			}
			
		}
		
		
		
		
		/*
		if (type){ //we want cosnt regime
			num = arr[0];
		}
		
		else if (!type){ //we want linear regime
			num = arr[1]; // this now holds base value
			
			x1 = arr[2];
			x2 = arr[3];
			x3 = arr[4];
		}
		*/
		
		
		globalMatrix1[row][0] = bank1;
		globalMatrix1[row][1] = bank2;
		globalMatrix1[row][2] = bank3;
		globalMatrix1[row][3] = bank4;
		globalMatrix1[row][4] = bank5;
		
		globalMatrix2[row][0] = total;
		
		//bankCreditors.add(new ArrayList<Integer>());
		
		ArrayList<Integer> hold = new ArrayList<Integer>();
		
		for (int i = 0; i < globalMatrix1[row].length; i++){
			if (globalMatrix1[row][i] == -1){
				hold.add(i+1);
			}
		}
		
		bankCreditors.add(hold);

		
		
		//System.out.println(globalMatrix1[row][0] + "\t" + globalMatrix1[row][1] + "\t" + globalMatrix1[row][2] + "\t" + globalMatrix1[row][3] + "\t" + globalMatrix1[row][4]);	
		//System.out.println(globalMatrix2[row][0]);
		
	}
	
	
	
	public static void MatrixSolution(){ //what regimes are we in?
		
		/*
		 * A set of arrays that will hold both the solvent and insolvent values for each bank.
		 * arr[0] is always = const, arr[1] is always = linear eqn
		 * 
		 * Maybe this could be refactored into a getInput function?
		 */
		
		
		
		//double[][] arr1 = { {1, -1, 0}, {0, 1, -1}, {-1, 0, 1}}; 
		
		Matrix A = new Matrix(globalMatrix1);
		
		//double[][] arr2 = { {7}, {9}, {4}};
		
		Matrix B = new Matrix(globalMatrix2);
		if (globalCount == 32){
		for (int c1 = 0; c1 < 5; c1++){
			//for (int c2 = 0; c2 < 5; c2++){
				//System.out.println(globalMatrix2[c1][0]);
			//}
		}
		}
			try{
				
				double gamma = 1.0; //a scalar to force a solution to the singularity
				boolean solved = true; //we want to set to false if no solution
				
			//	if (A.det() == 0) { System.out.println("DET = 0"); gamma = 0.8;}
				
				A = A.times(gamma);
				
				Matrix result = A.solve(B);
				//System.out.println("PASSED!");
				
				double [][] resultArr = result.getArray();
				
				double orig1 = allPayments[0]; //orig amount of money that was owed for bank1
				double orig2 = allPayments[1];
				double orig3 = allPayments[2];
				
				//System.out.println(orig1 + "\t" + orig2 + "\t"  + orig3);
				
				//if a bank receives more money than it is owed
				
				if ((resultArr[0][0] > orig1) || (resultArr[1][0] > orig2 || (resultArr[2][0] > orig3))) {
					//System.out.println("NOT A SOLUTION!");
					solved = false;
					}
				
				boolean bankBool1 = matrixArray[0];
				boolean bankBool2 = matrixArray[1];
				boolean bankBool3 = matrixArray[2];
				
				
				/*
				if (globalCount == 4){
					
					System.out.println(bankBool1);
					System.out.println(bankBool2);
					System.out.println(bankBool3);
					
				}
				*/
				//System.out.println(solved);	
				
				//if you're solvent AND the amount of money that you ended up having was less than what was required to be paid, then again discard
				
				//need to solve for all solvent case
				if ((bankBool1 && (resultArr[0][0] < orig1)) || (bankBool2 && (resultArr[1][0]  < orig2)) || (bankBool3 && (resultArr[0][0] < orig3))){
					solved = false;
					
					//if more money was paid that what a bank had
				}
				double cash1 = 0.0;
				double cash2 = 0.0;
				double cash3 = 0.0;
				
				ArrayList<Integer> a1 = bankCreditors.get(0);
				ArrayList<Integer> a2 = bankCreditors.get(1);
				ArrayList<Integer> a3 = bankCreditors.get(2);
				
				
					for (int index : a1){
						cash1 = cash1 + resultArr[index-1][0];
					}
					
					for (int index : a2){
						cash2 = cash2 + resultArr[index-1][0];
					}
					
					for (int index : a3){
						cash3 = cash3 + resultArr[index-1][0];
					}
					
					cash1 = cash1 + baseAmounts[0]; 
					cash2 = cash2 + baseAmounts[1]; 
					cash3 = cash3 + baseAmounts[2]; 
					//write algorithm to determine if bank is solvent or not using the fact that we have all the 
					// banks that owe a particular bank cash. ALSO TEST
					
					if ((bankBool1 && (cash1 < orig1)) || (bankBool2 && (cash2  < orig2)) || (bankBool3 && (cash3 < orig3))){
						solved = false;
						
						//if more money was paid that what a bank had
					}
				
				
				
				
				if (globalCount == 8){
					/*
					for (int ind : a1){
						System.out.println(ind);
					}
					*/
					//System.out.println(cash1 + "\t" + orig1);
					//System.out.println(cash2 + "\t" + orig2);
					//System.out.println(cash3 + "\t" + orig3);
					
				}
				
					
					
				//otherwise, if you were insolvent and the amount of money that you paid was less than what was 
				//required, then this state is acceptable; however you were INSOLVENT
				
				else if ((resultArr[0][0] < orig1) || (resultArr[1][0] < orig2) || (resultArr[2][0] < orig3)) {
					//System.out.print("INSOLVENT! \t ");
					if (resultArr[0][0] < orig1) failureCount++;
					if (resultArr[1][0] < orig2) failureCount++;
					if (resultArr[2][0] < orig3) failureCount++;
				}
				
			//	if (globalCount == 4) System.out.println("Solved is: " + solved);
				
				
				solved = consistencyAlgorithms(resultArr);
				
				
			
				//else System.out.println("SOLVED THE PROBLEM!"); //this is at the very end
				
				
			//RESULTS OF ALL CALCULATIONS
			System.out.print(solved + "\t");
			System.out.println(resultArr[0][0] + "\t" + resultArr[1][0] + "\t" + resultArr[2][0] + "\t" + resultArr[3][0] + "\t" + resultArr[4][0]);
			
				
				
			//RHS OF EQUATION
			//System.out.println(globalMatrix2[0][0] + "\t" + globalMatrix2[1][0] + "\t" + globalMatrix2[2][0]+ "\t" + globalMatrix2[3][0]+ "\t" + globalMatrix2[4][0]);
				
				
				
				
				//System.out.println(allPayments[0] + "\t" + allPayments[1] + "\t" + allPayments[2]);
				
			}
			
			catch(ArrayIndexOutOfBoundsException e){
				//System.out.println("ARRAY IS OUT OF BOUNDS");
				e.printStackTrace();
			}
			
			catch (Exception e){
				//e.printStackTrace();
				System.out.println("Singularity!");
				
				
				
			}
		
		
	}
	
	public static boolean consistencyAlgorithms(double resultArr[][]){
		
		boolean acceptable = true;
		
		for (int i = 0; i < 5; i++){ //
			
			//double bankID = allConnections[i][0][0];
			double [][] twoArr = allConnections[i];
			
			for (int j = 1; j < twoArr.length; j++){
				
				double bankID = twoArr[j][0];
				double min = twoArr[j][1];
				double base = twoArr[j][2];
				
				double payment = resultArr[(int) bankID-1][0];
				if (payment < 0) acceptable = false; //no negative results allowed
				
				double isSolvent = solvencyMap.get(bankID);
				
				boolean solv; 
				if (isSolvent == 1.0) solv = true;
				else solv = false;
				
				//The two test cases
				if (!solv && (base + payment > min)) {acceptable = false;} 
				if ((solv) && (base + payment < min)) {acceptable = false;}
				
				if (acceptable){
					if ((!solv) && (base + payment < min)){
						bankFailure.put(bankID, false);
					}
					else bankFailure.put(bankID, true);
				}
				//do something here
			}
			
		}
		return acceptable;
		
	}
	
	
	
	public static void main(String [] args){
	  	
		Integer count = new Integer(0);
		
		for (int i0 = 0; i0 < 32; i0++){
			
			String bitString = Integer.toBinaryString(count);
			
			int bitLength = bitString.length();
			
			//System.out.println(bitString);
			//System.out.println(bitLength);
			
			int extra = numberOfBanks - bitLength;
			
			for (int i = 0; i < extra; i++){
			
				bitString = "0".concat(bitString);
			}
				
			//System.out.println(bitString);
			char[] bitArr = bitString.toCharArray();
			//System.out.println(bitArr);
			
			//Now need to setup the true/false array
			
			
			
			double bankBool1 ; //0 means that it is insolvent; 1 means that it is solvent
			double bankBool2;
			double bankBool3;
			double bankBool4;
			double bankBool5;
			
			if (bitArr[0] == '0') bankBool1 = 0;
			else bankBool1 = 1;
			//matrixArray[0] = bankBool1;
			
			if (bitArr[1] == '0') bankBool2 = 0;
			else bankBool2 = 1;
			//matrixArray[1] = bankBool2;
			
			if (bitArr[2] == '0') bankBool3 = 0;
			else bankBool3 = 1;
			//matrixArray[2] = bankBool3;
			
			if (bitArr[3] == '0') bankBool4 = 0;
			else bankBool4 = 1;
			//matrixArray[3] = bankBool4;
			
			if (bitArr[4] == '0') bankBool5 = 0;
			else bankBool5 = 1;
			//matrixArray[4] = bankBool5;
		
		// new encoding for banks
		double [][] bankArr1 = { {1}, {2, 6, 9, bankBool2} }; 
		double [][] bankArr2 = { {2}, {3, 4, 2, bankBool3} };
		double [][] bankArr3 = { {3}, {1, 10, 7, bankBool1} , {5, 8, 1, bankBool5} };
		double [][] bankArr4 = { {4}, {1, 1, 7, bankBool1}, {2, 4, 9, bankBool2} };
		double [][] bankArr5 = { {5}, {4, 13, 11, bankBool4} };
		
		
		
		bArr1 = bankArr1;
		bArr2 = bankArr2;
		bArr3 = bankArr3;
		bArr4 = bankArr4;
		bArr5 = bankArr5;
		
		allConnections[0] = bArr1;
		allConnections[1] = bArr2;
		allConnections[2] = bArr3;
		allConnections[3] = bArr4;
		allConnections[4] = bArr5;
		
		allDebts[0] = debtArr1;
		allDebts[1] = debtArr2;
		allDebts[2] = debtArr3;
		allDebts[3] = debtArr4;
		allDebts[4] = debtArr5;
		
		//Do I still need these?
		int bankNum1 = 1;
		int bankNum2 = 2;
		int bankNum3 = 3;
		int bankNum4 = 4;
		int bankNum5 = 5;
		
		int row0 = 0;
		int row1 = 1;
		int row2 = 2;
		int row3 = 3;
		int row4 = 4;
		
		if (globalCount == 2){	
		
			
			MatrixSetup(bankNum1, bankArr1, row0); //false means that we are in linear regime
			MatrixSetup(bankNum2, bankArr2, row1);
			MatrixSetup(bankNum3, bankArr3, row2);
			MatrixSetup(bankNum4, bankArr4, row3);
			MatrixSetup(bankNum5, bankArr5, row4);
			
			/*
			for (double a = 1; a <= 5; a++){
				
				System.out.println(solvencyMap.get(a));
				
			}
			*/
			
			MatrixSolution();
			
		//	System.out.println(Integer.toBinaryString(count));
				
			
		}
		globalCount++;
		count++;
			
		}
		
		/*
		System.out.println();
		for (double d = 1; d <= 5; d++){
			System.out.println(d + "\t" + bankFailure.get(d));
		}
		*/
		//System.out.println();
		//System.out.println("Number of failed banks: " + failureCount);
		
		
		
	
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		for (int i = 0; i < 3; i++){
			//System.out.println(globalMatrix1[1][i]);
		}

		for (int i = 0; i < 3; i++){
			//System.out.println(globalMatrix2[i][0]);
		}
		
		
		
		
		
		
		/*
		int test = 3;
		
		switch(test){
			
		case 1:
			System.out.println("It is 1!");
			break;
			
		case 2:
			System.out.println("It is 2!");
			break;
		
		}
		*/
		//MatrixSetup();
		
		
		
		
	}
	
	
	
	
	
	

}

