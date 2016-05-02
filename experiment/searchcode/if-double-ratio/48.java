package RandomConnections;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Random;

import Jama.Matrix;

/*
 * 	Points: 1) We need to automate the connections/linking process
 * 			2) From the GUI, users can input lambda value, shock magnitude, also symmetry value
 * 			3) For those two parameters, it generates # of failures
 * 	
 * 			Symmetry value: how identical are bank assets (eg does everyone have exact same cash, or is there a
 * 			large discrepency 
 * 
 * 
 * 
 */





public class Model2 {
	
	//static ArrayList<Bank> allBanks = new ArrayList<Bank>();
	 //stores whether bank failed or not
	
	//-------------------------------------------------------------------------------------------------------//
	
	
	
	
	static double[][] bArr1;
	static double[][] bArr2;
	static double[][] bArr3;
	static double[][] bArr4;
	static double[][] bArr5;
	static double[][] bArr6;
	static double[][] bArr7;
	static double[][] bArr8;
	
	
	static double [][] debtArr1 = new double[30][30];    //{ {-1.}, {-1.,-1.},{-1.,-1.}};		//  = { {1}, {3, 10}, {4, 1} };
	static double [][] debtArr2 = new double[30][30];        //{ {-1.}, {-1.,-1.},{-1.,-1.}}; 		//  = { {2}, {1, 6}, {4, 4} };
	static double [][] debtArr3 = new double[30][30];     //{ {-1.}, {-1.,-1.} }; 		//  = { {3}, {2, 4} };
	static double [][] debtArr4 = new double[30][30];    //{ {-1.}, {-1.,-1.} }; 		//  = { {4}, {5, 13} };
	static double [][] debtArr5 = new double[30][30];  //  { {-1.}, {-1.,-1.} }; 		//  = { {5}, {3, 8} };
	static double [][] debtArr6 = new double[30][30];
	static double [][] debtArr7 = new double[30][30];
	static double [][] debtArr8 = new double[30][30];
	
	static double [][][] allConnections = new double[25][25][25];
	static double [][][] allDebts = new double[25][25][25];
	static double [] allBases = new double[20];
	static double [] allBools = new double[20];
	
	static Integer numberOfBanks = 20;
	static Integer powerBanks = (int)Math.pow(2, numberOfBanks);
	
	static double [] allBanks = new double[numberOfBanks];
	
	static double[][] globalMatrix1 = new double[numberOfBanks][numberOfBanks]; //the LHS of the eqn
	static double[][] globalMatrix2 = new double[numberOfBanks][1]; //RHS
	
	static HashMap<Double, Double> solvencyMap = new HashMap<Double, Double>();
	static HashMap<Double, Boolean> bankFailure = new HashMap<Double, Boolean>();
	
	
	static double [][] bankArr1;
	static double [][] bankArr2;
	static double [][] bankArr3;
	static double [][] bankArr4;
	static double [][] bankArr5;
	static double [][] bankArr6;
	static double [][] bankArr7;
	static double [][] bankArr8;
	static double [][] bankArr9;
	static double [][] bankArr10;
	static double [][] bankArr11;
	static double [][] bankArr12;
	static double [][] bankArr13;
	static double [][] bankArr14;
	static double [][] bankArr15;
	static double [][] bankArr16;
	static double [][] bankArr17;
	static double [][] bankArr18;
	static double [][] bankArr19;
	static double [][] bankArr20;
	
	static int globalCount = 1; //for debug purposes
	
	static double bankBool1 ; //0 means that it is insolvent; 1 means that it is solvent
	static double bankBool2;
	static double bankBool3;
	static double bankBool4;
	static double bankBool5;
	static double bankBool6;
	static double bankBool7;
	static double bankBool8;
	static double bankBool9;
	static double bankBool10;
	static double bankBool11;
	static double bankBool12;
	static double bankBool13;
	static double bankBool14;
	static double bankBool15;
	static double bankBool16;
	static double bankBool17;
	static double bankBool18;
	static double bankBool19;
	static double bankBool20;
	
	
	static double base1 = 7;
	static double base2 = 9;
	static double base3 = 2;
	static double base4 = 10;
	static double base5 = 1;
	static double base6 = 3;
	static double base7 = 4;
	static double base8 = 9;
	static double base9 = 2;
	static double base10 = 4;
	static double base11 = 4;
	static double base12 = 5;
	static double base13 = 9;
	static double base14 = 7;
	static double base15 = 10;
	static double base16 = 1;
	static double base17 = 13;
	static double base18 = 6;
	static double base19 = 9;
	static double base20 = 12;
	
	
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
	
	public static void debtSetup(){
		
		for (int i = 0; i < allConnections.length; i++){
			for (int j = 1; j < allConnections[i].length; j++){
				
				int srcBank = (int)allConnections[i][j][0]; //place the source bank here
				
				double[] src = {srcBank};
				
				/*
				System.out.println("i = " + i);
				System.out.println("j = " + j);
				System.out.println();
				*/
				if(srcBank == 0.0) break;
				allDebts[srcBank-1][0] = src;
				
				for (int k = 1; k < allDebts[srcBank-1].length; k++){ //what are the bounds of k?
				
					/*
					if (srcBank==3) {
						System.out.println("k outside loop = " + k);
						
					}
					*/
					//System.out.println("i = " + i);
					//System.out.println("j = " + j);
					//System.out.println("k = " + k);
					
					
					if (allDebts[srcBank-1][k][0] == 0.0){ //then there is no entry here
						double temp[] = new double[2];
						temp[0] = allConnections[i][0][0];
						temp[1] = allConnections[i][j][1];
						
						allDebts[srcBank-1][k] = temp;
						
						//allDebts[srcBank-1][k][0] = allConnections[i][0][0]; //place the destBank here
						//allDebts[srcBank-1][k][1] = allConnections[i][j][1]; //place the min amount here 
						/*
						if (srcBank==3){
						System.out.println("srcBank = " + srcBank);
						System.out.println("k = " + k);
						System.out.println();
						}
						*/
						break;
						
					}
				}
			}
		}
		
	}
	
	
	public static void MatrixSetup(int bankNum, double [][] arr, int row){
		
		//allPayments[row] = arr[0]; // this saves the payment value for later comparison
		//baseAmounts[row] = arr[1]; //saves base amount per bank
		
		double num = 0 ; //this will hold either the const value or base value
		
		int count = 0;
		
		
		for (double[] x : arr){
			double bankID = x[0];
			double bankBool;
			if (count>0){ 
				bankBool = allBools[(int)bankID-1];
				solvencyMap.put(bankID, bankBool);
			}
			count++;
		}
		
		
		double bank1 = 0;
		double bank2 = 0;
		double bank3 = 0;
		double bank4 = 0;
		double bank5 = 0;
		double bank6 = 0;
		double bank7 = 0;
		double bank8 = 0;
		
		for (int q =0; q < allBanks.length; q++){
			allBanks[q] = 0;
		}
		
		double min1 = 0;
		double min2 = 0;
		double min3 = 0;
		double min4 = 0;
		double min5 = 0;
		
		ArrayList<Double> allDebtors = new ArrayList<Double>();
		
		int destBank = (int)arr[0][0];
		
		allBanks[destBank-1] = 1;
		
		
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
		case 6:
			bank6 = 1;
			break;
		case 7:
			bank7 = 1;
			break;
		case 8:
			bank8 = 1;
			break;
		}
		
		double total = 0; //this will calc RHS
		
		
		for (int c = 1; c < arr.length; c++){
			
			int sourceBank = (int)arr[c][0];
			double min = arr[c][1];
			double base = arr[c][2];
			double type = allBools[sourceBank-1]; //  arr[c][3];
			
			//System.out.println(sourceBank + " is solvent = " + type);
			
			double [][] debtArray = allDebts[sourceBank-1]; 
			
			double totalPay = 0;
			double thisPay = 0;
			
			for (int p = 1; p < debtArray.length; p++){
				
				if(debtArray[p][0] != 0.0){
				
				totalPay = totalPay + debtArray[p][1];
				
				if (debtArray[p][0] == bankNum){
					thisPay = debtArray[p][1];
				}
			}
			}
			
			double ratio = thisPay/totalPay;
			
			//System.out.println(totalPay);
			
			//ratio = 1;
			
			if (type == 0){ //insolvent
				double temp = base*ratio;
				total += temp;
				
				//total += base;
				//total = total*ratio;
				
				//if (row == 3) System.out.println(total);
				
				allBanks[sourceBank - 1] = -1*ratio;
				
				/*
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
				case 6:
					bank6 = -1*ratio;
					min5 = min;
					break;
				case 7:
					bank7 = -1*ratio;
					min5 = min;
					break;
				case 8:
					bank8 = -1*ratio;
					min5 = min;
					break;
				
				}
				*/
				
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
		
		
		for (int i = 0; i < numberOfBanks; i++){
			globalMatrix1[row][i] = allBanks[i];
		}
		
		globalMatrix2[row][0] = total;
		
		/*
		globalMatrix1[row][0] = allBanks[0];
		globalMatrix1[row][1] = allBanks[1];
		globalMatrix1[row][2] = allBanks[2];
		globalMatrix1[row][3] = allBanks[3];
		globalMatrix1[row][4] = allBanks[4];
		globalMatrix1[row][5] = allBanks[5];
		globalMatrix1[row][6] = allBanks[6];
		globalMatrix1[row][7] = allBanks[7];
		
		
		*/
		
		
		
		
		//System.out.println("Banks: " + bank1 + "\t" + bank2 + "\t" + bank3 + "\t" + bank4 + "\t" + bank5 + "\t" + bank6 + "\t" + bank7 + "\t" + bank8);
		//System.out.println("Array: " + allBanks[0] + "\t" + allBanks[1] + "\t" + allBanks[2] + "\t" + allBanks[3] + "\t" + allBanks[4] + "\t" + allBanks[5] + "\t" + allBanks[6] + "\t"+ allBanks[7]);
		
		//System.out.println();
		
		
		
		
		
		
		//bankCreditors.add(new ArrayList<Integer>());
		/*
		ArrayList<Integer> hold = new ArrayList<Integer>();
		
		for (int i = 0; i < globalMatrix1[row].length; i++){
			if (globalMatrix1[row][i] == -1){
				hold.add(i+1);
			}
		}
		
		bankCreditors.add(hold);
*/
		
		
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
		
		/*
		if (globalCount == 32){
		for (int c1 = 0; c1 < 5; c1++){
			//for (int c2 = 0; c2 < 5; c2++){
				//System.out.println(globalMatrix2[c1][0]);
			//}
		}
		}
		
		*/
		
			try{
				
				double gamma = 1.0; //a scalar to force a solution to the singularity
				boolean solved = true; //we want to set to false if no solution
				
			//	if (A.det() == 0) { System.out.println("DET = 0"); gamma = 0.8;}
				
				A = A.times(gamma);
				
				Matrix result = A.solve(B);
				//System.out.println("PASSED!");
				
				double [][] resultArr = result.getArray();
				
				//System.out.println(orig1 + "\t" + orig2 + "\t"  + orig3);
				
				//if a bank receives more money than it is owed
				
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
				/*
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
				
				
				
				
					
				//otherwise, if you were insolvent and the amount of money that you paid was less than what was 
				//required, then this state is acceptable; however you were INSOLVENT
				
				else if ((resultArr[0][0] < orig1) || (resultArr[1][0] < orig2) || (resultArr[2][0] < orig3)) {
					//System.out.print("INSOLVENT! \t ");
					if (resultArr[0][0] < orig1) failureCount++;
					if (resultArr[1][0] < orig2) failureCount++;
					if (resultArr[2][0] < orig3) failureCount++;
				}
				
			//	if (globalCount == 4) System.out.println("Solved is: " + solved);
				*/
				
				solved = consistencyAlgorithms(resultArr);
				
				
			
				//else System.out.println("SOLVED THE PROBLEM!"); //this is at the very end
				
			if (solved) { //solved	
			//RESULTS OF ALL CALCULATIONS
			System.out.print(solved + "\t");
			for(int x = 0; x < resultArr.length; x++){
				System.out.print(resultArr[x][0] + "\t" );
			}
			System.out.print(globalCount);
			System.out.println();
			
			}
			
			//System.out.println(resultArr[0][0] + "\t" + resultArr[1][0] + "\t" + resultArr[2][0] + "\t" + resultArr[3][0] + "\t" + resultArr[4][0]);
			
				
				
			//RHS OF EQUATION
			//System.out.println(globalMatrix2[0][0] + "\t" + globalMatrix2[1][0] + "\t" + globalMatrix2[2][0]+ "\t" + globalMatrix2[3][0]+ "\t" + globalMatrix2[4][0]);
				
				
				
				
				//System.out.println(allPayments[0] + "\t" + allPayments[1] + "\t" + allPayments[2]);
				
			}
			
			catch(ArrayIndexOutOfBoundsException e){
				//System.out.println("ARRAY IS OUT OF BOUNDS");
				e.printStackTrace();
			}
			
			catch(NullPointerException e){
				e.printStackTrace();
			}
			
			catch (Exception e){
			//	e.printStackTrace();
				System.out.println("Singularity: " + A.det());
				
				
				
			}
		
		
	}
	
	public static boolean consistencyAlgorithms(double resultArr[][]){
		
		boolean acceptable = true;
		
		for (int i = 0; i < numberOfBanks; i++){ //
			
			//double bankID = allConnections[i][0][0];
			double [][] twoArr = allConnections[i];
			
			for (int j = 1; j < twoArr.length; j++){
				
				double bankID = twoArr[j][0];
				double min = twoArr[j][1];
				double base = twoArr[j][2];
				
				double payment = resultArr[(int) bankID-1][0];
				if (payment < 0) acceptable = false; //no negative results allowed
				
				double isSolvent = -1.0;
				if (solvencyMap.containsKey(bankID)){
				isSolvent = solvencyMap.get(bankID);
				}
				
				if (!solvencyMap.containsKey(bankID)) System.out.println("NOT IN SOLVENCY MAP!!!");
				
				boolean solv; 
				if (isSolvent == 1.0) solv = true;
				else solv = false;
				
				//System.out.println(bankID + " is solvent = " + solv );
				
				//The two test cases
				if (!solv && (base + payment >= min)) {acceptable = false;} 
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
	
	//-------------------------------------------------------------------------------------------------------//
	/*
	 * This part of the program "wraps" all the previous methods. To start with, we will define addBank(), which will
	 * enable a user to add a bank with specific parameters and connections
	 * 
	 */
	
	
	public static void initializeBanks(){
		
		// new encoding for banks
				
			 bankArr1 = new double[][] {{1}};
			 bankArr2 = new double[][] {{2}};
			 bankArr3 = new double[][] {{3}};
			 bankArr4 = new double[][] {{4}};
			 bankArr5 = new double[][] {{5}};
			 bankArr6 = new double[][] {{6}};
			 bankArr7 = new double[][] {{7}};
			 bankArr8 = new double[][] {{8}};
			 bankArr9 = new double[][] {{9}};
			 bankArr10 = new double[][] {{10}};
			 bankArr11 = new double[][] {{11}};
			 bankArr12 = new double[][] {{12}};
			 bankArr13 = new double[][] {{13}};
			 bankArr14 = new double[][] {{14}};
			 bankArr15 = new double[][] {{15}};
			 bankArr16 = new double[][] {{16}};
			 bankArr17 = new double[][] {{17}};
			 bankArr18 = new double[][] {{18}};
			 bankArr19 = new double[][] {{19}};
			 bankArr20 = new double[][] {{20}};
		
		
				 allConnections[0] = bankArr1;
				 allConnections[1] = bankArr2;
				 allConnections[2] = bankArr3;
				 allConnections[3] = bankArr4;
				 allConnections[4] = bankArr5;
				 allConnections[5] = bankArr6;
				 allConnections[6] = bankArr7;
				 allConnections[7] = bankArr8;
				 allConnections[8] = bankArr9;
				 allConnections[9] = bankArr10;
				 allConnections[10] = bankArr11;
				 allConnections[11] = bankArr12;
				 allConnections[12] = bankArr13;
				 allConnections[13] = bankArr14;
				 allConnections[14] = bankArr15;
				 allConnections[15] = bankArr16;
				 allConnections[16] = bankArr17;
				 allConnections[17] = bankArr18;
				 allConnections[18] = bankArr19;
				 allConnections[19] = bankArr20;
				 
				 // set all bases to a constant
				 for (int i = 0; i < 20; i++){
					 allBases[i] = 5; 
				 }
				 
	}
	
	public static void addCreditor(int source, int destination, double amount){
		double base = allBases[source-1];
		double[][] arr = allConnections[destination - 1];
		int length = arr.length;
		if (length!=0) {
			double[][] arr0 = new double[length+1][4];
			arr0[0][0] = arr[0][0];
			for (int i = 1; i < length; i++){
				arr0[i][0] = arr[i][0];
				arr0[i][1] = arr[i][1];
				arr0[i][2] = arr[i][2];
				arr0[i][3] = arr[i][3];
			}
			double bool = allBools[source-1];
			double[] arr1 = {source, amount, base, bool};
			arr0[length] = arr1;
			allConnections[destination-1] = arr0;
		}
		else System.out.println("length is 0!");
	}
	
	public static void addNumberOfCreditors(int num, double debt){

		int count0 = 1;
		
		int start = 2;
		int stop = start + num;
			
		for (int k = 1; k <= 20; k++){ // this is for each outer bank
			for (int i = start; i < stop; i++){
				if (i > 20){
					i = 1;
					start = start - 20;
					stop = stop - 20;
				}
				if (count0!=i) 
					addCreditor(i, k, debt);
				else {
					start++;
					stop++;
				}
			}
			start = start + num;
			stop = stop + num;
			/*
			if (stop > 20){
				start = 1;
				stop = start + num;
			}
			*/
			count0++;
		}
		
		for (int y = 0; y < 20; y++){
			for (int x = 1; x <= num; x++){
				System.out.print(allConnections[y][x][0] + " ");
			}
			System.out.println();
		}
			
	
	}
	
	
	
	
	public void setBools(){ //this must be called at the appropriate time or all hell will break loose
		
		for (int i = 0; i < allConnections.length; i++){
			for (int j = 1; j < allConnections[i].length; j++){
				
			}
			
			
			
		}
		
		
		
	}
	
	public static void randomShock(){
		
		Random r = new Random();
		int i =  r.nextInt(2);
		base4 = base4 - i;
		System.out.println("Random number is: " + i);
	}
	
	
	
	
	
	
	
	
	
	
	//-----------------------------------------------------------------------------------------------------------//
	
	public static void main(String [] args){
	
		double start = System.currentTimeMillis();
		
		boolean setup = false;
		
		Integer count = new Integer(0);
	
	System.out.println("Start of main");	
		
	initializeBanks();
	addNumberOfCreditors(20, 1.5);
	
	double setupTime = System.currentTimeMillis() - start;
	
		for (int i0 = 0; i0 < powerBanks; i0++){
			
		
			String bitString = Integer.toBinaryString(count);
			
			int bitLength = bitString.length();
			
			//System.out.println(bitString);
			//System.out.println(bitLength);
			
			int extra = numberOfBanks - bitLength;
			
			for (int i = 0; i < extra; i++){
			
				bitString = "0".concat(bitString);
			}
				
		//	System.out.println(bitString);
			char[] bitArr = bitString.toCharArray();
			//System.out.println(bitArr);
			
			//Now need to setup the true/false array
			
			
			
			
			
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
			
			if (bitArr[5] == '0') bankBool6 = 0;
			else bankBool6 = 1;
			
			if (bitArr[6] == '0') bankBool7 = 0;
			else bankBool7 = 1;
			
			if (bitArr[7] == '0') bankBool8 = 0;
			else bankBool8 = 1;
			
			if (bitArr[8] == '0') bankBool9 = 0;
			else bankBool9 = 1;
			
			if (bitArr[9] == '0') bankBool10 = 0;
			else bankBool10 = 1;
		
			if (bitArr[10] == '0') bankBool11 = 0;
			else bankBool11 = 1;
		
			if (bitArr[11] == '0') bankBool12 = 0;
			else bankBool12 = 1;
		
			if (bitArr[12] == '0') bankBool13 = 0;
			else bankBool13 = 1;
		
			if (bitArr[13] == '0') bankBool14 = 0;
			else bankBool14 = 1;
		
			if (bitArr[14] == '0') bankBool15 = 0;
			else bankBool15 = 1;
			
			if (bitArr[15] == '0') bankBool16 = 0;
			else bankBool16 = 1;
			
			if (bitArr[16] == '0') bankBool17 = 0;
			else bankBool17 = 1;
			
			if (bitArr[17] == '0') bankBool18 = 0;
			else bankBool18 = 1;
			
			if (bitArr[18] == '0') bankBool19 = 0;
			else bankBool19 = 1;
			
			if (bitArr[19] == '0') bankBool20 = 0;
			else bankBool20 = 1;
		
			allBools[0] = bankBool1;
			allBools[1] = bankBool2;
			allBools[2] = bankBool3;
			allBools[3] = bankBool4;
			allBools[4] = bankBool5;
			allBools[5] = bankBool6;
			allBools[6] = bankBool7;
			allBools[7] = bankBool8;
			allBools[8] = bankBool9;
			allBools[9] = bankBool10;
			allBools[10] = bankBool11;
			allBools[11] = bankBool12;
			allBools[12] = bankBool13;
			allBools[13] = bankBool14;
			allBools[14] = bankBool15;
			allBools[15] = bankBool16;
			allBools[16] = bankBool17;
			allBools[17] = bankBool18;
			allBools[18] = bankBool19;
			allBools[19] = bankBool20;
			
			
			// Add banks 2 and 3 as creditors to 1 with amounts 3 and 7
			//addCreditor(2, 1, 3);
			//addCreditor(3, 1, 7);
			
			//Add bank
			
			//addCreditor(5, 2, 10);
			
			//addCreditors HAS to be called from below here:
			
			
			
		if (globalCount > 0){	
			
			/*
			
			
			 bankArr1 = new double[][] { {1}, {2, 6, base2, bankBool2}, {6, 4, base6, bankBool6}, {11, 2, base11, bankBool11 }, {13, 4, base13, bankBool13}, {4, 19, base4, bankBool4} }; 
			 bankArr2 = new double[][] { {2}, {3, 4, base3, bankBool3}, {13, 7, base13, bankBool13}, {18, 4, base18, bankBool18} };
			 bankArr3 = new double[][] { {3}, {1, 10, base1, bankBool1} , {5, 8, base5, bankBool5}, {8, 7, base8, bankBool8 }, {12, 4, base12, bankBool12}, {14, 3, base14, bankBool14} };
			 bankArr4 = new double[][] { {4}, {1, 1, base1, bankBool1}, {2, 4, base2, bankBool2}, {8, 3, base8, bankBool8 } };
			 bankArr5 = new double[][] { {5}, {4, 13, base4, bankBool4}, {9, 7, base9, bankBool9}, {10, 6, base10, bankBool10}  } ;                        // {5, 11, 9, bankBool5 } };
			 bankArr6 = new double[][] { {6}, {4, 3, base4, bankBool4}, {19, 4, base19, bankBool19}, {18, 3, base18, bankBool18}, {7, 3, base7, bankBool7} };
			 bankArr7 = new double[][] { {7}, {1, 2, base1, bankBool1}, {3, 5, base3, bankBool3}, {19, 2, base19, bankBool19} };
			 bankArr8 = new double[][] { {8}, {5, 2, base5, bankBool5}, {20, 5, base20, bankBool20 } };
			 bankArr9 = new double[][] { {9}, {1, 1, base1, bankBool1}, {10, 3, base10, bankBool10}, {11, 3, base11, bankBool11} };
			 bankArr10 = new double [][] { {10}, {3, 2, base3, bankBool3}, {12, 3, base12, bankBool12}, {15, 5, base15, bankBool15} };
			 bankArr11 = new double [][] { {11}, {10, 2, base10, bankBool10}, {16, 2, base16, bankBool16} };
			 bankArr12 = new double [][] { {12}, {4, 2, base4, bankBool4}, {19, 3, base19, bankBool19} };
			 bankArr13 = new double [][] { {13}, {8, 3, base8, bankBool8}, {20, 8, base20, bankBool20}, {4, 3, base4, bankBool4} };
			 bankArr14 = new double [][] { {14}, {2, 2, base2, bankBool2}, {15, 6, base15, bankBool15}, {4, 8, base4, bankBool4} };
			 bankArr15 = new double [][] { {15}, {13, 1, base13, bankBool13}, {20, 5, base20, bankBool20} };
			 bankArr16 = new double [][] { {16}, {4, 1, base4, bankBool4}, {9, 1, base9, bankBool9}, {11, 1, base11, bankBool11}, {12, 1, base12, bankBool12} };
			 bankArr17 = new double [][] { {17}, {12, 1, base12, bankBool12}, {11, 1, base11, bankBool11}, {14, 3, base14, bankBool14} };
			 bankArr18 = new double [][] { {18}, {15, 1, base15, bankBool15}, {4, 5, base4, bankBool4} };
			 bankArr19 = new double [][] { {19}, {17, 6, base17, bankBool17}, {4, 2, base4, bankBool4} };
			 bankArr20 = new double [][] { {20}, {17, 9, base17, bankBool17}, {4, 3, base4, bankBool4} };
		
			 /*
		bArr1 = bankArr1;
		bArr2 = bankArr2;
		bArr3 = bankArr3;
		bArr4 = bankArr4;
		bArr5 = bankArr5;
		bArr6 = bankArr6;
		bArr7 = bankArr7;
		bArr8 = bankArr8;
		
		
		allConnections[0] = bArr1;
		allConnections[1] = bArr2;
		allConnections[2] = bArr3;
		allConnections[3] = bArr4;
		allConnections[4] = bArr5;
		allConnections[5] = bArr6;
		allConnections[6] = bArr7;
		allConnections[7] = bArr8;
		
			 
		allConnections[0] = bankArr1;
		allConnections[1] = bankArr2;
		allConnections[2] = bankArr3;
		allConnections[3] = bankArr4;
		allConnections[4] = bankArr5;
		allConnections[5] = bankArr6;
		allConnections[6] = bankArr7;
		allConnections[7] = bankArr8;
		allConnections[8] = bankArr9;
		allConnections[9] = bankArr10;
		allConnections[10] = bankArr11;
		allConnections[11] = bankArr12;
		allConnections[12] = bankArr13;
		allConnections[13] = bankArr14;
		allConnections[14] = bankArr15;
		allConnections[15] = bankArr16;
		allConnections[16] = bankArr17;
		allConnections[17] = bankArr18;
		allConnections[18] = bankArr19;
		allConnections[19] = bankArr20;
		
		allBases[0] = base1;
		allBases[1] = base2;
		allBases[2] = base3;
		allBases[3] = base4;
		allBases[4] = base5;
		allBases[5] = base6;
		allBases[6] = base7;
		allBases[7] = base8;
		allBases[8] = base9;
		allBases[9] = base10;
		allBases[10] = base11;
		allBases[11] = base12;
		allBases[12] = base13;
		allBases[13] = base14;
		allBases[14] = base15;
		allBases[15] = base16;
		allBases[16] = base17;
		allBases[17] = base18;
		allBases[18] = base19;
		allBases[19] = base20;
		
		*/
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		if(!setup)	{
			debtSetup();
			setup = true;
			//System.out.println("In setup! Global count = " + globalCount);
		
		//System.out.println(allDebts[2][1][0]);
		
		/*
		allDebts[0] = debtArr1;
		allDebts[1] = debtArr2;
		allDebts[2] = debtArr3;
		allDebts[3] = debtArr4;
		allDebts[4] = debtArr5;
		*/
		
	
		
		}
		//System.out.println(allDebts[2][1][0]);
		
		//Do I still need these?
		
		for (int z = 0; z < numberOfBanks; z++){
			MatrixSetup(z+1, allConnections[z] ,z);
		}
		
		/*
		int bankNum1 = 1;
		int bankNum2 = 2;
		int bankNum3 = 3;
		int bankNum4 = 4;
		int bankNum5 = 5;
		int bankNum6 = 6;
		int bankNum7 = 7;
		int bankNum8 = 8;
		
		int row0 = 0;
		int row1 = 1;
		int row2 = 2;
		int row3 = 3;
		int row4 = 4;
		int row5 = 5;
		int row6 = 6;
		int row7 = 7;
			
			MatrixSetup(bankNum1, bankArr1, row0); //false means that we are in linear regime
			MatrixSetup(bankNum2, bankArr2, row1);
			MatrixSetup(bankNum3, bankArr3, row2);
			MatrixSetup(bankNum4, bankArr4, row3);
			MatrixSetup(bankNum5, bankArr5, row4);
			MatrixSetup(bankNum6, bankArr6, row5);
			MatrixSetup(bankNum7, bankArr7, row6);
			MatrixSetup(bankNum8, bankArr8, row7);
			
			MatrixSetup(9, bankArr9, 8);
			MatrixSetup(10, bankArr10, 9);
			MatrixSetup(11, bankArr11, 10);
			MatrixSetup(12, bankArr12, 11);
			MatrixSetup(13, bankArr13, 12);
			MatrixSetup(14, bankArr14, 13);
			MatrixSetup(15, bankArr15, 14);
			MatrixSetup(16, bankArr16, 15);
			
			MatrixSetup(17, bankArr17, 16);
			MatrixSetup(18, bankArr18, 17);
			MatrixSetup(19, bankArr19, 18);
			MatrixSetup(20, bankArr20, 19);
			
				
			/*
			for (double a = 1; a <= 5; a++){
				
				System.out.println(solvencyMap.get(a));
				
			}
			*/
			
			MatrixSolution();
			
			for (int n = 0; n < numberOfBanks; n++){
			//	System.out.println(globalMatrix2[n][0]);
			}
		}
		globalCount++;
		count++;
			
		}
		
		//System.out.println(globalCount);
		
	//	System.out.println(globalMatrix1[4][7]);
		
		//debtSetup();
		
		
		for (int i = 0; i < 5; i++){
			
			for (int j = 0; j < allDebts[i].length; j++){
				
			}
		
		
		}
		/*
		System.out.println();
		for (double d = 1; d <= 5; d++){
			System.out.println(d + "\t" + bankFailure.get(d));
		}
		*/
		//System.out.println();
	//	System.out.println("Number of failed banks: " + failureCount);
		
		
		
	
		
		
		
		
		
		
		
		
		
		
		
		
		
		/*
		for (int i = 0; i < 3; i++){
			//System.out.println(globalMatrix1[1][i]);
		}

		for (int i = 0; i < 3; i++){
			//System.out.println(globalMatrix2[i][0]);
		}
		*/
		
		
		
		
		
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
		
	/*
		for (int q = 0; q < 20; q++){
			System.out.println(allBases[q]);
		}
		*/
		
		double time2 = System.currentTimeMillis();
		
		System.out.println("Runtime = " + (time2 - start));
		
	}
	
	
	
	
	
	
	
	

}

