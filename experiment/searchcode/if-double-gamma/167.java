package X_GUI_Compatible1;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;

import Jama.Matrix;

/*
 * 	AS OF SATURDAY APRIL 12 THIS IS THE CODE THAT IS BEING USED!!!!!!!!!!!
 */



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
	
	static double[][] finalResult;
	
	
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
	
    static double [] allBools = new double[20];
	
	static boolean [] safe = new boolean[20];
	static boolean [] danger = new boolean[20];
	
	static Integer numberOfBanks = 13;
	static Integer powerBanks = (int)Math.pow(2, numberOfBanks);
	
	static double [] allBanks = new double[25];
	static double [] allBases = new double[25];
	
	static double[][] globalMatrix1 = new double[numberOfBanks][numberOfBanks]; //the LHS of the eqn
	static double[][] globalMatrix2 = new double[numberOfBanks][1]; //RHS
	
	static HashMap<Double, Double> solvencyMap = new HashMap<Double, Double>();
	static HashMap<Double, Boolean> bankFailure = new HashMap<Double, Boolean>();
	
	static HashMap<Integer, String> bankNames = new HashMap<Integer, String>();
		
	static int Market;
	
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
	
	/*
			allBases[0] = 9;
			allBases[1] = 131;
			allBases[0] = 34;
			allBases[0]
			allBases[0]
	*/
	
	
	static double base1 =  9; // 74;
	static double base2 =  131;     //241;
	static double base3 = 34;          //30;
	static double base4 = 76;       //274;
	static double base5 = 86;         //86;
	static double base6 = 171;          //169;
	static double base7 = 40;           //85;
	static double base8 = 24;              //22;
	static double base9 = 43;              //64;
	static double base10 = 137;                 //121;
	static double base11 = 4;                  //187;
	static double base12 = 1.3;             //59;
	static double base13 = 0.9;           //47;
	/*
	static double base14 = 7;
	static double base15 = 10;
	static double base16 = 1;
	static double base17 = 13;
	static double base18 = 6;
	static double base19 = 9;
	static double base20 = 12;
	*/
	
	//-------------------------------------------------------------------------------//
	
	static double[] allPayments = new double[5]; //keeps track of all payments
	
	
	static double[] baseAmounts = new double[5]; //keeps track of base amounts
	
	static ArrayList<ArrayList<Integer>> bankCreditors = new ArrayList<ArrayList<Integer>>(); 
	//an arrayList for storing the creditors for each bank. outer A_L: index starts at 0, inner : at 1 
	
	
	static boolean [] matrixArray = new boolean[3];
	

	
	static int failureCount = 0;
	
	
	
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
			count++;
			//System.out.println(count);
			double bankID = x[0];
			double bankBool = allBools[(int)bankID-1];
			solvencyMap.put(bankID, bankBool);
		}
		
		
		double bank1 = 0;
		double bank2 = 0;
		double bank3 = 0;
		double bank4 = 0;
		double bank5 = 0;
		double bank6 = 0;
		double bank7 = 0;
		double bank8 = 0;
		
		
		//hrere it is reset
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
		
		/*
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
		*/
		double total = 0; //this will calc RHS
		
		
		for (int c = 1; c < arr.length; c++){
			
			int sourceBank = (int)arr[c][0];
			double min = arr[c][1];
			double base = allBases[sourceBank-1];    //  arr[c][2];
			double type = allBools[sourceBank-1];
			
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
				
				allBanks[sourceBank - 1] = -1*ratio;
				
				
				//total += base;
				//total = total*ratio;
				
				//if (row == 3) System.out.println(total);
				
				
				
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
				finalResult = resultArr;
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
				e.printStackTrace();
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
				//double min = twoArr[j][1];
				double base = twoArr[j][2];
				
				double [][] debtArr = allDebts[(int)bankID-1];
				
				double min = 0;
				for (int x = 1; x < debtArr.length; x++){
					min += debtArr[x][1];
				}
				
				//System.out.println(bankID + "\t" + base);
				
				double payment = resultArr[(int) bankID-1][0];
				if (payment < 0) acceptable = false; //no negative results allowed
				
				double isSolvent = -1.0;
				if (solvencyMap.containsKey(bankID)){
					isSolvent = solvencyMap.get(bankID);
				}
				
				if (!solvencyMap.containsKey(bankID)) System.out.println("NOT IN SOLVENCY MAP!!!");
				
				boolean solv; 
				if (isSolvent == 1.0) solv = true;
				else if (isSolvent == 0.0) solv = false;
				else {
					solv = false;
					System.out.println("NOT IN SOLVENCY MAP!");
				}
				
				//The two test cases
				if (!solv && (base + payment >= min)) {acceptable = false;} 
				if ((solv) && (base + payment < min)) {acceptable = false;}
				
			//	if (bankID==1) System.out.println(base + "," + payment + "," + min);
				
				//System.out.println("acceptable is: " + acceptable);
				
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
	
	
public static void initializeBanks(int market){
	
	// new encoding for banks
	Market = market;
			
	allConnections = new double[25][25][25];
	allDebts = new double[25][25][25];
	
	solvencyMap = new HashMap<Double, Double>();
	bankFailure = new HashMap<Double, Boolean>();
	
	numberOfBanks = 13;
	powerBanks = (int)Math.pow(2, numberOfBanks);
	
	allBanks = new double[25]; //why does this even exist?
	
	allBases = new double[25];
	allBools = new double[20];
	
	safe = new boolean[20];
	danger = new boolean[20];
	
	globalCount = 1;
	failureCount = 0;
	
	globalMatrix1 = new double[numberOfBanks][numberOfBanks];
	globalMatrix2 = new double[numberOfBanks][1];
	
	if (market == 1){
	
	 bankArr1 = new double[][] { {1}, {2, 6.29, base2, bankBool2}, {3, 5.254, base3, bankBool3}, {4, 7.622, base4, bankBool4 }, {5, 4.292, base5, bankBool5}, {6, 7.252, base6, bankBool6}, {7, 6.512, base7, bankBool7}, {8, 3.774, base8, bankBool8}, {9, 5.92, base9, bankBool9}, {10, 5.772, base10, bankBool10}, {11, 2.368, base11, bankBool11}, {12, 1.924, base12, bankBool12}  , {13, 2.22, base13, bankBool13} }; 
	 bankArr2 = new double[][] { {2}, {1, 20.0003, base1, bankBool1}, {3, 14.46, base3, bankBool3}, {4, 25.546, base4, bankBool4}, {5, 13.978, base5, bankBool5}, {6, 19.28, base6, bankBool6}, {7, 17.834, base7, bankBool7}, {8, 14.701, base8, bankBool8}, {9, 17.111, base9, bankBool9}, {10, 22.172, base10, bankBool10}, {11, 10.122, base11, bankBool11}, {12, 8.435, base12, bankBool12} , {13, 11.086, base13, bankBool13} };
	 bankArr3 = new double[][] { {3}, {1, 2.52, base1, bankBool1} , {2, 2.49, base2, bankBool2}, {4, 2.52, base4, bankBool4 }, {5, 1.86, base5, bankBool5}, {6, 2.79, base6, bankBool6}, {7, 2.55, base7, bankBool7}, {8, 1.71, base8, bankBool8}, {9, 2.52, base9, bankBool9}, {10, 2.49, base10, bankBool10}, {11, 1.26, base11, bankBool11}, {12, 0.72, base12, bankBool12}, {13, 0.9, base13, bankBool13} };
	 bankArr4 = new double[][] { {4}, {1, 26.03, base1, bankBool1}, {2, 26.304, base2, bankBool2}, {3, 14.796, base3, bankBool3}, {5, 13.426, base5, bankBool5}, {6, 23.838, base6, bankBool6}, {7, 21.372, base7, bankBool7}, {8, 14.248, base8, bankBool8,}, {9, 19.18, base9, bankBool9}, {10, 21.92, base10, bankBool10}, {11, 14.796, base11, bankBool11}, {12, 9.59, base12, bankBool12}, {13, 12.878, base13, bankBool13} };
	 bankArr5 = new double[][] { {5}, {1, 7.052, base1, bankBool1}, {2, 7.396, base2, bankBool2}, {3, 5.848, base3, bankBool3}, {4, 6.536, base4, bankBool4,}, {6, 7.568, base6, bankBool6}, {7, 11.438, base7, bankBool7}, {8, 3.44, base8, bankBool8}, {9, 5.16, base9, bankBool9}, {10, 6.536, base10, bankBool10}, {11, 2.064, base11, bankBool11}, {12, 1.634, base12, bankBool12}, {13, 2.236, base13, bankBool13}  } ;                        // {5, 11, 9, bankBool5 } };
	 bankArr6 = new double[][] { {6}, {1, 17.238, base1, bankBool1}, {2, 14.534, base2, bankBool2}, {3, 11.999, base3, bankBool3}, {4, 17.914, base4, bankBool4}, {5, 10.478, base5, bankBool5}, {7, 16.055, base7, bankBool7}, {8, 8.788, base8, bankBool8}, {9, 13.182, base9, bankBool9}, {10, 12.337, base10, bankBool10}, {11, 6.084, base11, bankBool11}, {12, 4.225, base12, bankBool12}, {13, 4.394, base13, bankBool13} };
	 bankArr7 = new double[][] { {7}, {1, 7.82, base1, bankBool1}, {2, 7.055, base2, bankBool2}, {3, 6.035, base3, bankBool3}, {4, 7.565, base4, bankBool4} , {5, 8.33, base5, bankBool5}, {6, 8.245, base6, bankBool6}, {8, 3.57, base8, bankBool8}, {9, 4.675, base9, bankBool9}, {10, 6.035, base10, bankBool10}, {11, 2.89, base11, bankBool11}, {12, 2.38, base12, bankBool12}, {13, 3.06, base13, bankBool13} };
	 bankArr8 = new double[][] { {8}, {1, 1.694, base1, bankBool1}, {2, 1.936, base2, bankBool2 }, {3, 1.628, base3, bankBool3}, {4, 1.87, base4, bankBool4}, {5, 1.012, base5, bankBool5}, {6, 1.672, base6, bankBool6}, {7, 1.452, base7, bankBool7}, {9, 1.672, base9, bankBool9}, {10, 1.936, base10, bankBool10}, {11, 1.144, base11, bankBool11}, {12, 0.924, base12, bankBool12}, {13, 1.078, base13, bankBool13}  };
	 bankArr9 = new double[][] { {9}, {1, 5.952, base1, bankBool1}, {2, 6.336, base2, bankBool2}, {3, 4.864, base3, bankBool3}, {4, 6.336, base4, bankBool4}, {5, 3.648, base5, bankBool5}, {6, 5.568, base6, bankBool6}, {7, 4.096, base7, bankBool7}, {8, 3.456, base8, bankBool8}, {10, 5.44, base10, bankBool10}, {11, 2.752, base11, bankBool11}, {12, 1.024, base12, bankBool12}, {13, 1.1728, base13, bankBool13} };
	 bankArr10 = new double [][] { {10}, {1, 10.043, base1, bankBool1}, {2, 12.342, base2, bankBool2}, {3, 7.865, base3, bankBool3}, {4, 11.858, base4, bankBool4}, {5, 7.502, base5, bankBool5}, {6, 9.196, base6, bankBool6}, {7, 8.591, base7, bankBool7}, {8, 7.139, base8, bankBool8} , {9, 8.833, base9, bankBool9}, {11, 4.598, base11, bankBool11}, {12, 4.598, base12, bankBool12}, {13, 6.413, base13, bankBool13}   };
	 bankArr11 = new double [][] { {11}, {1, 9.911, base1, bankBool1}, {2, 13.651, base2, bankBool2}, {3, 9.163, base3, bankBool3}, {4, 16.456, base4, bankBool4}, {5, 4.862, base5, bankBool5}, {6, 9.724, base6, bankBool6}, {7, 9.163, base7, bankBool7}, {8, 11.594, base8, bankBool8}, {9, 11.22, base9, bankBool9}, {10, 10.472, base10, bankBool10}, {12, 12.342, base12, bankBool12}, {13, 16.83, base13, bankBool13} };
	 bankArr12 = new double [][] { {12}, {1, 2.478, base1, bankBool1}, {2, 3.186, base2, bankBool2}, {3, 1.475, base3, bankBool3}, {4, 3.54, base4, bankBool4}, {5, 1.357, base5, bankBool5}, {6, 2.065, base6, bankBool6}, {7, 2.242, base7, bankBool7}, {8, 3.245, base8, bankBool8}, {9, 1.121, base9, bankBool9}, {10, 4.012, base10, bankBool10}, {11, 3.835, base11, bankBool11}, {13, 12.98, base13, bankBool13} };
	 bankArr13 = new double [][] { {13}, {1, 2.021, base1, bankBool1}, {2, 2.961, base2, bankBool2}, {3, 1.363, base3, bankBool3}, {4, 3.055, base4, bankBool4}, {5, 1.222, base5, bankBool5}, {6, 1.551, base6, bankBool6}, {7, 1.927, base7, bankBool7}, {8, 2.444, base8, bankBool8}, {9, 1.363, base9, bankBool9}, {10, 3.431, base10, bankBool10}, {11, 3.478, base11, bankBool11},  {12, 8.272, base12, bankBool12}  }; //
		 
	 
	 
	 	bankArr14 = new double[][] {{14}};
		 bankArr15 = new double[][] {{15}};
		 bankArr16 = new double[][] {{16}};
		 bankArr17 = new double[][] {{17}};
		 bankArr18 = new double[][] {{18}};
		 bankArr19 = new double[][] {{19}};
		 bankArr20 = new double[][] {{20}};
		 
		 
		 allBases[0] = 9;
		 allBases[1] = 131;
		 allBases[2] = 34;
		 allBases[3] = 76;
		 allBases[4] = 86;
		 allBases[5] = 171;
		 allBases[6] = 40;
		 allBases[7] = 24;
		 allBases[8] = 43;
		 allBases[9] = 137;
		 allBases[10] = 4;
		 allBases[11] = 1.3;
		 allBases[12] = 0.9;
		 
	}
	
	else if (market == 2){
		
		bankArr1 = new double[][] { {1}, {2, 3.219, base2, bankBool2}, {3, 2.8712, base3, bankBool3}, {4, 4.329, base4, bankBool4 }, {5, 7.2446, base5, bankBool5}, {6, 6.2974, base6, bankBool6}, {7, 7.77, base7, bankBool7}, {8, 2.3088, base8, bankBool8}, {9, 5.8534, base9, bankBool9}, {10, 1.6058, base10, bankBool10}, {11, 7.7774, base11, bankBool11}, {12, 0, base12, bankBool12}  , {13, 0, base13, bankBool13} }; 
		 bankArr2 = new double[][] { {2}, {1, 10.2425, base1, bankBool1}, {3, 44.585, base3, bankBool3}, {4, 38.319, base4, bankBool4}, {5, 43.139, base5, bankBool5}, {6, 34.4871, base6, bankBool6}, {7, 45.6936, base7, bankBool7}, {8, 14.8697, base8, bankBool8}, {9, 14.5805, base9, bankBool9}, {10, 33.5472, base10, bankBool10}, {11, 48.9471, base11, bankBool11}  };
		 bankArr3 = new double[][] { {3}, {1, 2.895, base1, bankBool1} , {2, 2.448, base2, bankBool2}, {4, 1.641, base4, bankBool4 }, {5, 2.001, base5, bankBool5}, {6, 1.536, base6, bankBool6}, {7, 1.914, base7, bankBool7} };
		 bankArr4 = new double[][] { {4}, {1, 24.8792, base1, bankBool1}, {2, 24.6052, base2, bankBool2}, {3, 28.359, base3, bankBool3}, {5, 29.455, base5, bankBool5}, {6, 30.3318, base6, bankBool6}, {7, 23.6736, base7, bankBool7}, {8, 25.8382, base8, bankBool8,}, {9, 32.6882, base9, bankBool9}, {10, 26.1396, base10, bankBool10}, {11, 23.5366, base11, bankBool11} };
		 bankArr5 = new double[][] { {5}, {1, 3.6722, base1, bankBool1}, {2, 4.687, base2, bankBool2}, {3, 7.3186, base3, bankBool3}, {4, 7.6626, base4, bankBool4,}, {6, 12.04, base6, bankBool6}, {7, 12.728, base7, bankBool7}, {8, 3.9732, base8, bankBool8}, {9, 2.4596, base9, bankBool9}, {10, 3.8786, base10, bankBool10}, {11, 14.4652, base11, bankBool11} } ;                        // {5, 11, 9, bankBool5 } };
		 bankArr6 = new double[][] { {6}, {1, 15.6156, base1, bankBool1}, {2, 17.2718, base2, bankBool2}, {3, 14.365, base3, bankBool3}, {4, 16.2409, base4, bankBool4}, {5, 17.8633, base5, bankBool5}, {7, 20.9222, base7, bankBool7}, {8, 3.5152, base8, bankBool8}, {9, 14.3819, base9, bankBool9}, {10, 7.1825, base10, bankBool10}, {11, 27.1583, base11, bankBool11}, {12, 9.1429, base12, bankBool12} };
		 bankArr7 = new double[][] { {7}, {1, 7.497, base1, bankBool1}, {2, 8.398, base2, bankBool2}, {3, 9.044, base3, bankBool3}, {4, 6.3835, base4, bankBool4} , {5, 12,444, base5, bankBool5}, {6, 11.305, base6, bankBool6}, {8, 3.1365, base8, bankBool8}, {9, 4.148, base9, bankBool9}, {10, 2.533, base10, bankBool10}, {11, 13.107, base11, bankBool11} };
		 bankArr8 = new double[][] { {8}, {1, 1.4696, base1, bankBool1}, {2, 1.3904, base2, bankBool2 }, {3, 1.4586, base3, bankBool3}, {4, 1.3508, base4, bankBool4}, {5, 1.012, base5, bankBool5}, {6, 1.672, base6, bankBool6}, {7, 1.452, base7, bankBool7}, {9, 1.672, base9, bankBool9}, {10, 1.936, base10, bankBool10}, {11, 1.144, base11, bankBool11}, {12, 0.924, base12, bankBool12}, {13, 1.078, base13, bankBool13}  };
		 bankArr9 = new double[][] { {9}, {1, 3.1232, base1, bankBool1}, {2, 2.4576, base2, bankBool2}, {3, 2.592, base3, bankBool3}, {4, 4.0064, base4, bankBool4}, {5, 4.48, base5, bankBool5}, {6, 2.8416, base6, bankBool6}, {7, 1.3504, base7, bankBool7}, {8, 2.8608, base8, bankBool8}, {11, 1.2992, base11, bankBool11} };
		 bankArr10 = new double [][] { {10}, {1, 7.1995, base1, bankBool1}, {2, 3.9446, base2, bankBool2}, {3, 6.9454, base3, bankBool3}, {4, 3.4606, base4, bankBool4}, {5, 7.9981, base5, bankBool5}, {6, 2.9645, base6, bankBool6}, {7, 8.2764, base7, bankBool7}, {8, 3.4606, base8, bankBool8} , {9, 5.1183, base9, bankBool9}, {11, 5.7838, base11, bankBool11}};
		 bankArr11 = new double [][] { {11}, {1, 19.7659, base1, bankBool1}, {2, 15.895, base2, bankBool2}, {3, 20.7944, base3, bankBool3}, {4, 23.1506, base4, bankBool4}, {5, 34.2584, base5, bankBool5}, {6, 34.6511, base6, bankBool6}, {7, 38.0732, base7, bankBool7}, {8, 14.3429, base8, bankBool8}, {9, 14.2868, base9, bankBool9}, {10, 11.8932, base10, bankBool10}, {12, 41.14, base12, bankBool12}, {13, 31.79, base13, bankBool13} };
		 bankArr12 = new double [][] { {12},  {2, 7.4812, base2, bankBool2},  {8, 4.307, base8, bankBool8}, {9, 3.0267, base9, bankBool9}, {10, 3.3866, base10, bankBool10}, {11, 10.8619, base11, bankBool11}, {13, 12.9564, base13, bankBool13} };
		 bankArr13 = new double [][] { {13},  {2, 2.961, base2, bankBool2}, {8, 2.452, base8, bankBool8}, {9, 3.478, base9, bankBool9}, {10, 2.5568, base10, bankBool10}, {11, 9.541, base11, bankBool11},  {12, 9.5175, base12, bankBool12}  }; //
			 
		
		 
		 allBases[0] = 9;
		 allBases[1] = 131;
		 allBases[2] = 34;
		 allBases[3] = 76;
		 allBases[4] = 86;
		 allBases[5] = 171;
		 allBases[6] = 40;
		 allBases[7] = 24;
		 allBases[8] = 43;
		 allBases[9] = 137;
		 allBases[10] = 4;
		 allBases[11] = 1.3;
		 allBases[12] = 0.9;
		 
		
	}
		 
		 
		 bankNames.clear();
		 bankNames.put(1, "AXP");
		 bankNames.put(2, "BAC");
		 bankNames.put(3, "BK");
		 bankNames.put(4, "C");
		 bankNames.put(5, "GS");
		 bankNames.put(6, "JPM");
		 bankNames.put(7, "MS");
		 bankNames.put(8, "PNC");
		 bankNames.put(9, "USB");
		 bankNames.put(10, "WFC");
		 bankNames.put(11, "AIG");
		 bankNames.put(12, "FNM");
		 bankNames.put(13, "FRE");
		 
		 
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
			 /*
			 for (int i = 0; i < 20; i++){
				 allBases[i] = 53; 
			 }
			 */
}
	
	public static void addBank(String name, double base){
		
		if (numberOfBanks == 20) throw new NullPointerException(); //bounds checking
		
		numberOfBanks++;
		powerBanks = (int)Math.pow(2, numberOfBanks );
		//increase power
		
		allBases[numberOfBanks-1] = base;
		
		globalMatrix1 = new double[numberOfBanks][numberOfBanks];
		globalMatrix2 = new double[numberOfBanks][1];
		
		bankNames.put(numberOfBanks, name);
		
		
	}
	
	public static void addCreditor(int destination, int source, double amount){
		
		if (source < 1 || source > numberOfBanks){
			throw new IllegalArgumentException();
		}
		
		if (destination < 1 || destination > numberOfBanks){
			throw new IllegalArgumentException();
		}
		
		double base = allBases[source-1];
		double[][] arr = allConnections[destination - 1];
		
		for (int r = 1; r < arr.length; r++){
			if (arr[r][0] == source){
				double[] nArr = {source, amount, base, 0};
				allConnections[destination-1][r] = nArr;
				return;
			}
		}
		
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
	
	public static void deleteCreditor(int destination, int source, boolean fixed){
		
		double [][] arr = allConnections[destination-1]; 
		
		int length = arr.length;
		if (length < 2){
			System.out.println("Cant remove creditor");
			return;
		}
		
		double [][] replicate = new double[arr.length-1][4];
		
		boolean exists = false;
		for (double[] subArr : arr ){
			if (subArr[0]==source){
				exists = true;
			}
		}
		
		if (!exists) replicate = new double[arr.length][4];
		
		int i = 0;
		boolean flag = false;
		for (double [] subArr : arr ){
			
			if (subArr[0] == source) flag = true;
			
			if (subArr[0] != source) {
				replicate[i] = subArr;
				if (flag && !fixed) replicate[i][0] = replicate[i][0] - 1;
				i++;
			}
		}
		allConnections[destination-1] = replicate;
		
	}
	
	public static void deleteBank(String bankName){
		
		// First remove from HashMap:
		int bankID = -1;
		for (int i = 1; i <= numberOfBanks; i++){
			if (bankNames.get(i).equals(bankName)) bankID = i;
		}
		
	//	System.out.print(bankID + " ");
		if (bankID < 1 || bankID > numberOfBanks){
			throw new IllegalArgumentException();
		}
		
		for (int i = bankID; i < numberOfBanks; i++){
			String temp = bankNames.get(i+1);
			
			bankNames.put(i, temp);
		}
		bankNames.remove(numberOfBanks);
				
				
		
		// for causing creditors to drop
		/*
		for (int i = 0; i < numberOfBanks; i++){
			boolean flag = false;
			for (int  j = 1; j < allConnections[i].length-1; j++){
				
				if (flag) {
					allConnections[i][j] = allConnections[i][j+1];
					allConnections[i][j][0] = allConnections[i][j][0] - 1;
				}
				
				if (!flag){
					if (allConnections[i][j][0] == bankID) flag = true;
				}
					
			}
		}
		*/
		
		for (int i = 0; i < numberOfBanks; i++){
			
			double [][] temp = allConnections[i];

			int destination = (int)temp[0][0];
			//System.out.println(destination);
			if (destination!=bankID)
			deleteCreditor(destination, bankID, false);
		}
			
		double[][][] tempConnections = new double[25][25][25];
		
		// for causing all banks to drop one place
		int c = 0;
		boolean flag = false;
		for (int i = 0; i < numberOfBanks; i++){
			//System.out.println(allConnections[i][0][0] + " , " + bankID );
			if (allConnections[i][0][0]==bankID) flag = true;
			
			if (allConnections[i][0][0] != bankID){
				tempConnections[c] = allConnections[i];
				if (flag){tempConnections[c][0][0] = tempConnections[c][0][0] - 1;}
				c++;
			}
		}
		
		numberOfBanks = numberOfBanks - 1; //where should this line be?????
		powerBanks = (int)Math.pow(2, numberOfBanks);
		
		
		allConnections = tempConnections;
		//double [][]delArr = {{bankID}};
		//allConnections[bankID-1] = delArr;
		
		/*
		for (int i = 0; i < numberOfBanks; i++){
			for (int j = 0; j < allConnections[i].length; j++){
				System.out.print(allConnections[i][j][0] + " ");
			}
			System.out.println();
		}
		
		
		for (int i = 0; i < numberOfBanks; i++){
			for (int j = 1; j < allConnections[i].length; j++){
				System.out.print("{" + allConnections[i][j][0] + ", " + allConnections[i][j][1] + ", " + allConnections[i][j][2] + "}  ");
			}
			System.out.println();
		}
		*/
		
		globalMatrix1 = new double[numberOfBanks][numberOfBanks];
		globalMatrix2 = new double[numberOfBanks][1];
		
	}
	
	
	public static void applyShock(double shock, int bankID){
		allBases[bankID] = allBases[bankID] - shock;
	}
	
	
	
	
	
	
	
	//-----------------------------------------------------------------------------------------------------------//
	
	public static void main(String [] args){
	  	for (int x = 0; x <= 0; x++){
		boolean setup = false;
		boolean cont;
		
		Integer count = new Integer(0);
	
	System.out.println("Start of main");	
	
	//initializeBanks();
	
	//addBank(5);
	
//	addBank(4);
	
//	addCreditor(14, 2, 60);
	
	
	//addCreditor(15, 2, 30);
	//addCreditor(2, 15, 5);
	//addCreditor(15, 3, 20);
	
	
	System.out.println("Number of banks: " + numberOfBanks);
	
	//addCreditor(1, 14, 6);
	
	debtSetup();
	
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
			
			
			
			/*
			
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
		
			/*
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
		*/
			
			
		if (globalCount > 0){	 
			/*
			 bankArr1 = new double[][] { {1}, {2, 6.29, base2, bankBool2}, {3, 5.254, base3, bankBool3}, {4, 7.622, base4, bankBool4 }, {5, 4.292, base5, bankBool5}, {6, 7.252, base6, bankBool6}, {7, 6.512, base7, bankBool7}, {8, 3.774, base8, bankBool8}, {9, 5.92, base9, bankBool9}, {10, 5.772, base10, bankBool10}, {11, 2.368, base11, bankBool11}, {12, 1.924, base12, bankBool12}, {13, 2.22, base13, bankBool13} }; 
			 bankArr2 = new double[][] { {2}, {1, 20.0003, base1, bankBool1}, {3, 14.46, base3, bankBool3}, {4, 25.546, base4, bankBool4}, {5, 13.978, base5, bankBool5}, {6, 19.28, base6, bankBool6}, {7, 17.834, base7, bankBool7}, {8, 14.701, base8, bankBool8}, {9, 17.111, base9, bankBool9}, {10, 22.172, base10, bankBool10}, {11, 10.122, base11, bankBool11}, {12, 8.435, base12, bankBool12}, {13, 11.086, base13, bankBool13} };
			 bankArr3 = new double[][] { {3}, {1, 2.52, base1, bankBool1} , {2, 2.49, base2, bankBool2}, {4, 2.52, base4, bankBool4 }, {5, 1.86, base5, bankBool5}, {6, 2.79, base6, bankBool6}, {7, 2.55, base7, bankBool7}, {8, 1.71, base8, bankBool8}, {9, 2.52, base9, bankBool9}, {10, 2.49, base10, bankBool10}, {11, 1.26, base11, bankBool11}, {12, 0.72, base12, bankBool12}, {13, 0.9, base13, bankBool13} };
			 bankArr4 = new double[][] { {4}, {1, 26.03, base1, bankBool1}, {2, 26.304, base2, bankBool2}, {3, 14.796, base3, bankBool3}, {5, 13.426, base5, bankBool5}, {6, 23.838, base6, bankBool6}, {7, 21.372, base7, bankBool7}, {8, 14.248, base8, bankBool8,}, {9, 19.18, base9, bankBool9}, {10, 21.92, base10, bankBool10}, {11, 14.796, base11, bankBool11}, {12, 9.59, base12, bankBool12}, {13, 12.878, base13, bankBool13} };
			 bankArr5 = new double[][] { {5}, {1, 7.052, base1, bankBool1}, {2, 7.396, base2, bankBool2}, {3, 5.848, base3, bankBool3}, {4, 6.536, base4, bankBool4,}, {6, 7.568, base6, bankBool6}, {7, 11.438, base7, bankBool7}, {8, 3.44, base8, bankBool8}, {9, 5.16, base9, bankBool9}, {10, 6.536, base10, bankBool10}, {11, 2.064, base11, bankBool11}, {12, 1.634, base12, bankBool12}, {13, 2.236, base13, bankBool13}  } ;                        // {5, 11, 9, bankBool5 } };
			 bankArr6 = new double[][] { {6}, {1, 17.238, base1, bankBool1}, {2, 14.534, base2, bankBool2}, {3, 11.999, base3, bankBool3}, {4, 17.914, base4, bankBool4}, {5, 10.478, base5, bankBool5}, {7, 16.055, base7, bankBool7}, {8, 8.788, base8, bankBool8}, {9, 13.182, base9, bankBool9}, {10, 12.337, base10, bankBool10}, {11, 6.084, base11, bankBool11}, {12, 4.225, base12, bankBool12}, {13, 4.394, base13, bankBool13} };
			 bankArr7 = new double[][] { {7}, {1, 7.82, base1, bankBool1}, {2, 7.055, base2, bankBool2}, {3, 6.035, base3, bankBool3}, {4, 7.565, base4, bankBool4} , {5, 8.33, base5, bankBool5}, {6, 8.245, base6, bankBool6}, {8, 3.57, base8, bankBool8}, {9, 4.675, base9, bankBool9}, {10, 6.035, base10, bankBool10}, {11, 2.89, base11, bankBool11}, {12, 2.38, base12, bankBool12}, {13, 3.06, base13, bankBool13} };
			 bankArr8 = new double[][] { {8}, {1, 1.694, base1, bankBool1}, {2, 1.936, base2, bankBool2 }, {3, 1.628, base3, bankBool3}, {4, 1.87, base4, bankBool4}, {5, 1.012, base5, bankBool5}, {6, 1.672, base6, bankBool6}, {7, 1.452, base7, bankBool7}, {9, 1.672, base9, bankBool9}, {10, 1.936, base10, bankBool10}, {11, 1.144, base11, bankBool11}, {12, 0.924, base12, bankBool12}, {13, 1.078, base13, bankBool13}  };
			 bankArr9 = new double[][] { {9}, {1, 5.952, base1, bankBool1}, {2, 6.336, base2, bankBool2}, {3, 4.864, base3, bankBool3}, {4, 6.336, base4, bankBool4}, {5, 3.648, base5, bankBool5}, {6, 5.568, base6, bankBool6}, {7, 4.096, base7, bankBool7}, {8, 3.456, base8, bankBool8}, {10, 5.44, base10, bankBool10}, {11, 2.752, base11, bankBool11}, {12, 1.024, base12, bankBool12}, {13, 1.1728, base13, bankBool13} };
			 bankArr10 = new double [][] { {10}, {1, 10.043, base1, bankBool1}, {2, 12.342, base2, bankBool2}, {3, 7.865, base3, bankBool3}, {4, 11.858, base4, bankBool4}, {5, 7.502, base5, bankBool5}, {6, 9.196, base6, bankBool6}, {7, 8.591, base7, bankBool7}, {8, 7.139, base8, bankBool8} , {9, 8.833, base9, bankBool9}, {11, 4.598, base11, bankBool11}, {12, 4.598, base12, bankBool12}, {13, 6.413, base13, bankBool13}   };
			 bankArr11 = new double [][] { {11}, {1, 9.911, base1, bankBool1}, {2, 13.651, base2, bankBool2}, {3, 9.163, base3, bankBool3}, {4, 16.456, base4, bankBool4}, {5, 4.862, base5, bankBool5}, {6, 9.724, base6, bankBool6}, {7, 9.163, base7, bankBool7}, {8, 11.594, base8, bankBool8}, {9, 11.22, base9, bankBool9}, {10, 10.472, base10, bankBool10}, {12, 12.342, base12, bankBool12}, {13, 16.83, base13, bankBool13} };
			 bankArr12 = new double [][] { {12}, {1, 2.478, base1, bankBool1}, {2, 3.186, base2, bankBool2}, {3, 1.475, base3, bankBool3}, {4, 3.54, base4, bankBool4}, {5, 1.357, base5, bankBool5}, {6, 2.065, base6, bankBool6}, {7, 2.242, base7, bankBool7}, {8, 3.245, base8, bankBool8}, {9, 1.121, base9, bankBool9}, {10, 4.012, base10, bankBool10}, {11, 3.835, base11, bankBool11}, {13, 12.98, base13, bankBool13} };
			 bankArr13 = new double [][] { {13}, {1, 2.021, base1, bankBool1}, {2, 2.961, base2, bankBool2}, {3, 1.363, base3, bankBool3}, {4, 3.055, base4, bankBool4}, {5, 1.222, base5, bankBool5}, {6, 1.551, base6, bankBool6}, {7, 1.927, base7, bankBool7}, {8, 2.444, base8, bankBool8}, {9, 1.363, base9, bankBool9}, {10, 3.431, base10, bankBool10}, {11, 3.478, base11, bankBool11}, {12, 8.272, base12, bankBool12}  };
			 
			 /*
			 
			 bankArr14 = new double [][] { {14}, {2, 2, base2, bankBool2}, {15, 6, base15, bankBool15}, {4, 8, base4, bankBool4} };
			 bankArr15 = new double [][] { {15}, {13, 1, base13, bankBool13}, {20, 5, base20, bankBool20} };
			 bankArr16 = new double [][] { {16}, {4, 1, base4, bankBool4}, {9, 1, base9, bankBool9}, {11, 1, base11, bankBool11}, {12, 1, base12, bankBool12} };
			 bankArr17 = new double [][] { {17}, {12, 1, base12, bankBool12}, {11, 1, base11, bankBool11}, {14, 3, base14, bankBool14} };
			 bankArr18 = new double [][] { {18}, {15, 1, base15, bankBool15}, {4, 5, base4, bankBool4} };
			 bankArr19 = new double [][] { {19}, {17, 6, base17, bankBool17}, {4, 2, base4, bankBool4} };
			 bankArr20 = new double [][] { {20}, {17, 9, base17, bankBool17}, {4, 3, base4, bankBool4} };
		*/
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
		*/
			 /*
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
		*/
		/*
		allConnections[13] = bankArr14;
		allConnections[14] = bankArr15;
		allConnections[15] = bankArr16;
		allConnections[16] = bankArr17;
		allConnections[17] = bankArr18;
		allConnections[18] = bankArr19;
		allConnections[19] = bankArr20;
			*/ 
		/*
		if(!setup)	{
			debtSetup();
			setup = true;
			*/
			//System.out.println("In setup! Global count = " + globalCount);
		
		//System.out.println(allDebts[2][1][0]);
		
		/*
		allDebts[0] = debtArr1;
		allDebts[1] = debtArr2;
		allDebts[2] = debtArr3;
		allDebts[3] = debtArr4;
		allDebts[4] = debtArr5;
		*/
		
	
		
		
		//System.out.println(allDebts[2][1][0]);
		
		//Do I still need these?
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
			
		

		cont = false;
		
		for (int i = 0; i < numberOfBanks; i++){
			if (bitArr[i] == '0') {
				if (safe[i]) cont = true;
				allBools[i] = 0;
			}
			else {
				if (danger[i]) cont = true;
				allBools[i] = 1;
			}
		}
		

		for (int z = 0; z < numberOfBanks; z++){
			MatrixSetup(z+1, allConnections[z], z);
		}
		
		/*
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
			*/
				
			/*
			for (double a = 1; a <= 5; a++){
				
				System.out.println(solvencyMap.get(a));
				
			}
			*/
			
			MatrixSolution();
			
			for (double n = 1; n <= numberOfBanks; n++){
				//System.out.println(n + "\t" + solvencyMap.get(n));
			}
		}
		globalCount++;
		count++;
			
		}
		
		//System.out.println(globalCount);
		
	//	System.out.println(globalMatrix1[4][7]);
		
		//debtSetup();
		
		
		String failed = "Failed banks:\t";
		for (double d = 1.0; d <= numberOfBanks; d++){
			
			if (bankFailure.get(d)!=null){
			if(!bankFailure.get(d)) {
				failureCount++;
				failed = failed.concat((int)d + "\t");
			}
		}
		}
		System.out.println("Number of failed banks: " + failureCount);
		System.out.println(failed);
		
		/*
		for (int i = 0; i < numberOfBanks; i++){
			
			System.out.println(globalMatrix2[i][0]);
			
			
		}
		*/
		
		
		
		
		
		
		
		
		
		
		
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
		
		initializeBanks(Market);
		
	  	}	
	}
	
	
	
	
	
	

}

