package com.teamtyro.src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

import com.teamtyro.etc.BetterConstants;
import com.teamtyro.etc.MazeMap;


public class ReadSolutions {

	public String[] humanSolutions;		//Holds a string of each solution
	public String[] geneticSolutions;	//Holds a string of each solution that will be tested.
	public String[] neuralSolutions;	//Holds a string of each solution that will be tested.
	//public static String solutionsFile = "mapsolutions.txt";
	//public static String testSolutionsFile = "testSolutions.txt";
	
	public static double outputs[][];
	public static MazeMap m = new MazeMap();
	public static int[][] map;
	public Random r = new Random();//3
	public static String mapnumber;
	public static int sX;				//The x start position of the player
	public static int sY;				//The y start position of the player
	public static BetterConstants constant = new BetterConstants();

	public ReadSolutions(String humanSolutionsFile, String geneticSolutionsFile,String neuralSolutionsFile){											//the name of the map file.
		map = new int[BetterConstants.MAP_WIDTH][BetterConstants.MAP_HEIGHT];
		getmapArray();
		//System.out.println("////SOLUTION READER////");
		humanSolutions = readSolutions(humanSolutionsFile);	//Sets the solutions array to all of the data in the text file. solutions[solution#] = String of solution
		geneticSolutions = readSolutions(geneticSolutionsFile);
		neuralSolutions = readSolutions(neuralSolutionsFile);
	}

	public static String[] readSolutions(String file){
		int solutionsCount = 0;									//How many solutions there are.
		int totalMoves = 0;									//Total amount of moves from each solution added up.

		try {												//Gets the amount of solutions, so that I can make an array and record them all as a String.
			BufferedReader in = new BufferedReader(new FileReader(file));
			String line = "";
			while ((line = in.readLine()) != null) {		//If there is still a line, then add to int solutions.
				totalMoves += line.length();
				solutionsCount += 1;
			}
			in.close();
			//System.out.println("	Total Moves: "+totalMoves+"	Total Solutions: "+solutionsCount);	
		} catch(IOException ex) {
			System.out.printf("ERROR: Couldn't load map\n");
		}

		String[] textSolutions = new String[solutionsCount];		//Now that we know the total solutions, create an array of that size.

		try {												//Records the solutions as String onto the solutions[] array.

			BufferedReader in = new BufferedReader(new FileReader(file));
			for(int s = 0; s < solutionsCount; s++) {			//Goes through each line, and records down the solutions in order into the textSolutions array.
				//System.out.println(line);
				textSolutions[s] = in.readLine();
			}

			in.close();
		} catch(IOException ex) {
			System.out.printf("ERROR: Couldn't load maphhh\n");
		}

/*		for(int s = 0; s < solutions; s++){					//For testing, print out each solution in the solution array.
			System.out.println(textSolutions[s]);
		}*/
		//System.out.println("///////////////////////"+"\n");	//The \n will skip to a line.
		return textSolutions;
	}

	
	public int[][][][][][] getRawInputArray(String[] solutionSet, boolean includeLastOutput){
		int[][][][][][] tallyInputs	= new int[2][2][2][2][4][4];;

		for(int s = 0; s < solutionSet.length; s++){
			for(int l = 0; l < solutionSet[s].length(); l++){
				
				int[] sol = getSituation(solutionSet[s],l, includeLastOutput);
				int numericalOutput = 0;
				if(solutionSet[s].charAt(l) == 'u'){ numericalOutput = 0;}
				if(solutionSet[s].charAt(l) == 'd'){ numericalOutput = 1;}
				if(solutionSet[s].charAt(l) == 'l'){ numericalOutput = 2;}
				if(solutionSet[s].charAt(l) == 'r'){ numericalOutput = 3;}
				
				if(includeLastOutput){
					tallyInputs[sol[0]][sol[1]][sol[2]][sol[3]][sol[4]][numericalOutput] += 1;		//Adds 1 to the tally for the given output of that person, for that given input set.
				}else{
					tallyInputs[sol[0]][sol[1]][sol[2]][sol[3]][0][numericalOutput] += 1;		//Adds 1 to the tally for the given output of that person, for that given input set.
				}
				
			}
		}
		
		return tallyInputs;
	}
	
	
	
	public int[][][] getMapPercent(String[] solutionSet){							//Gets Map[x][y] and [0-3] of which direction, returns the percent of people who went that way in that particular spot.
		double[][][] mapPercent = new double[BetterConstants.MAP_WIDTH][BetterConstants.MAP_HEIGHT][4];
		int[][][] percent = new int[BetterConstants.MAP_WIDTH][BetterConstants.MAP_HEIGHT][4];
		
		for(int i = 0; i < solutionSet.length; i++){
			for(int j = 0; j < solutionSet[i].length(); j++){
				int[] coordinates = getCoordinates(solutionSet[i], j);	//Gets the [0] = x, and the [1] = y of the position where the person did that particular move.
				
				if(getNumericalOutput(solutionSet[i].charAt(j)) != -1){
					mapPercent[coordinates[0]][coordinates[1]][getNumericalOutput(solutionSet[i].charAt(j))] += 1;	//Adds one to that direction at that coordinate.
				}else{
					System.out.println("Serious error. getNumericalOutput returned a -1");
				}
				
			}
		}
		
		
		for(int x = 0; x < mapPercent.length; x++){								//Converts it to a percent
			for(int y = 0; y < mapPercent[x].length; y++){
				
				double total = 0;
				for(int direction = 0; direction <= 3; direction++){			//Finds the total tallies, in order to divide each possible solution's tally by total tallys.
					percent[x][y][direction]= 0;
					total += mapPercent[x][y][direction];
				}
				if(total != 0){								//So that I don't divide by zero...
					for(int direction = 0; direction <= 3; direction++){		//Goes through each out, and sets it as the percent of the total. Cool simple code, me gusta! p.s. I am so tired. It is finals week, and I should not be coding. Well. Toodleoo! To the next line I go...
						//percent[in0][in1][in2][in3][in4][in5] = round((double) tally[in0][in1][in2][in3][in4][in5]/total*100,2,BigDecimal.ROUND_HALF_UP);
						percent[x][y][direction] = (int) ((mapPercent[x][y][direction]/total)*100);
						//System.out.println(percent[x][y][direction]);
					}
				}
				
				
			}
		}
		
		
		return percent;
	}
	
	public static int[] getCoordinates(String solution, int move){					//Gets [0] = x, [1] = y
		int[] coordinates = new int[2];
		
		int rightMoves = 0;
		int leftMoves = 0;
		int upMoves = 0;
		int downMoves = 0;

		for(int m = 0; m < move; m++){
			if(solution.charAt(m) == 'r'){	rightMoves += 1;}
			if(solution.charAt(m) == 'l'){	leftMoves += 1;	}
			if(solution.charAt(m) == 'u'){	upMoves += 1;	}
			if(solution.charAt(m) == 'd'){	downMoves += 1;	}
		}
		int pX = sX + (rightMoves - leftMoves);	//finds the player position at that time.
		int pY = sY + (downMoves  - upMoves	);	//Finds the player position at that time.
		
		coordinates[0] = pX;
		coordinates[1] = pY;
		
		return coordinates;
	}
	
	public static int getNumericalOutput(char s){										//Returns 0 for up, 1 for down, 2 for left, 3 for right
		int numericalOutput = -1;
		if(s == 'u'){ numericalOutput = 0;}
		if(s == 'd'){ numericalOutput = 1;}
		if(s == 'l'){ numericalOutput = 2;}
		if(s == 'r'){ numericalOutput = 3;}
		return numericalOutput;
	}
	
	public static double[][][][][][] getPercent(int[][][][][][] tally){											//Fills up the percent array with processed data from the tally array.
		double[][][][][][] percent = new double[2][2][2][2][4][4];
		
		for(int in0 = 0; in0 <= 1; in0++){								//in0	block above
			for(int in1 = 0; in1 <= 1; in1++){							//in1	block below
				for(int in2 = 0; in2 <=1; in2++){						//in2	block left
					for(int in3 = 0; in3 <=1; in3++){					//in3	block right
						for(int in4 = 0; in4 <= 3; in4++){				//in4	last move (up,down,left, or right)
							
							double total = 0;
							for(int in5 = 0; in5 <= 3; in5++){			//Finds the total tallies, in order to divide each possible solution's tally by total tallys.
								percent[in0][in1][in2][in3][in4][in5] = 0;
								total += tally[in0][in1][in2][in3][in4][in5];
							}
							if(total != 0){								//So that I don't divide by zero...
								for(int in5 = 0; in5 <= 3; in5++){		//Goes through each out, and sets it as the percent of the total. Cool simple code, me gusta! p.s. I am so tired. It is finals week, and I should not be coding. Well. Toodleoo! To the next line I go...
									//percent[in0][in1][in2][in3][in4][in5] = round((double) tally[in0][in1][in2][in3][in4][in5]/total*100,2,BigDecimal.ROUND_HALF_UP);
									percent[in0][in1][in2][in3][in4][in5] = (tally[in0][in1][in2][in3][in4][in5]/total)*100;
								}
							}
							
							
						}
					}
				}
			}
		}
		return percent;
	}

	public static int[] getSituation(String solution, int move, boolean includeLastMove){							//Gets the inputs at the time that a particular move was performed, in the String solution.
		int[] situation;
		if(includeLastMove){
			situation = new int[5];	//Order: [0] = up, [1] = down, [2] = left, [3] = right, [4] = lastOutput. 0 = open block, 1 = filled block.
		}else{
			situation = new int[4];
		}
		
		int rightMoves = 0;
		int leftMoves = 0;
		int upMoves = 0;
		int downMoves = 0;

		for(int m = 0; m < move; m++){
			if(solution.charAt(m) == 'r'){	rightMoves += 1;}
			if(solution.charAt(m) == 'l'){	leftMoves += 1;	}
			if(solution.charAt(m) == 'u'){	upMoves += 1;	}
			if(solution.charAt(m) == 'd'){	downMoves += 1;	}
		}
		int pX = sX + (rightMoves - leftMoves);	//finds the player position at that time.
		int pY = sY + (downMoves  - upMoves	);	//Finds the player position at that time.

		if(pY + 1 < BetterConstants.MAP_HEIGHT){	//If you're not at the bottom of the map.
			situation[1] = map[pX][pY+1];	//below you
		}else{ situation[1] = BetterConstants.MAP_BLOCK;	}	

		if(pY - 1 >= 0){						//If you're not at the top of the map.
			situation[0] = map[pX][pY-1];	//above you
			if(situation[0] == BetterConstants.MAP_START){ situation[0] = BetterConstants.MAP_SPACE; }
		}else{	situation[0] = BetterConstants.MAP_BLOCK;}	

		if(pX + 1 < BetterConstants.MAP_WIDTH){	//If you're not at the right edge of the map.
			situation[3] = map[pX+1][pY];	//right of you
		}else{	situation[3] = BetterConstants.MAP_BLOCK;}	

		if(pX - 1 >= 0){						//If you're not at the left edge of the map.	
			situation[2] = map[pX-1][pY];	//left of you
			if(situation[2] == BetterConstants.MAP_WIN){ situation[2] = BetterConstants.MAP_SPACE; }
		}else{	situation[2] = BetterConstants.MAP_BLOCK;}	

		
		if(includeLastMove){
			if(move > 0){								//Finds the last move. Is recorded as: NO LAST MOVE = 0; 0 =u; 1/3=d; 2/3=l; 1=r
				situation[4] = solution.charAt(move-1);
				if(solution.charAt(move-1) == 'u'){	situation[4] = BetterConstants.DIR_UP;}
				if(solution.charAt(move-1) == 'd'){	situation[4] = BetterConstants.DIR_DOWN;}
				if(solution.charAt(move-1) == 'l'){	situation[4] = BetterConstants.DIR_LEFT;}
				if(solution.charAt(move-1) == 'r'){	situation[4] = BetterConstants.DIR_RIGHT;}
			}else{
				situation[4] = BetterConstants.DIR_UP;
			}
		}
		//System.out.println("Solution: " +solution);
		//System.out.println("Move: "+move+" "+solution.charAt(move)+" "+"	Up: "+situation[0]+"	Down: "+situation[1]+"	Left: "+situation[2]+"	Right: "+situation[3]+"	LastMove: "+situation[4]);
		//System.out.println("pXpY("+pX+","+pY+")"+"	pXpY("+sX+","+sY+")");
		return situation;
	}

	
	public static void getmapArray(){		//Sets the map[][] array to the same map[][] array in the main MazeGame.
		m.loadConstMap("cbbbccccccccbbbbcccccbbbbbbcbbbbcbbbcbsbccbcbbbbcccbccc" +
				"bccccbbbbcbcbbbcbbbcbbbbbcbcbbbcbcccbbbbbcccccccccbbbbbbbcbbbbbbb" +
				"bbbcbbbbccccccccccbcbbbbbbbbbcbbbcccbbbbcccbbcccccccbbbbcbccccbcc" +
				"cbcbbbbcbbbbbbcbcbcbbbbcbwcccbcbcbcbbbbcbbbbcbbbcbcbbbbcccccccccc" +
				"ccbbbb");
		for(int x=0; x<BetterConstants.MAP_WIDTH; x++) {		
			for(int y=0; y<BetterConstants.MAP_HEIGHT; y++) {
				map[x][y] = m.getSpace(x,y);
				if(map[x][y] == BetterConstants.MAP_START) {
					sX = x;
					sY = y;
					//System.out.println("Player X: "+sX+"	Player Y: "+sY);
				}
			}
		}	


	}

}
