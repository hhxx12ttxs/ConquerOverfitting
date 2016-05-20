
package scramble;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

/**
 * This class contains the dictionary, the matrix, and performs the search.
 * @author Zachary T Buckley
 */
public class Scramble {
	
	/**
	 * char array used to store the generated characters
	 */
	public char[][] matrix;
	
	/**
	 * Trie structure used to store the library of words in ram for quick access 
	 */
	public Trie words;
	
	/**
	 * LinkedList structure used to store the words found on the Scramble board
	 */
	public LinkedList<String> validwords;
	
	/**
	 * static variable limiting the number of threads to prevent memory issues
	 * if memory issues occur, consider decreasing this value.
	 */
	private final int MAX_THREADS = 512;
	
	/**
	 * private boolean for tracking print resulting words option
	 */
	private static boolean print_words = false;
	
	/**
	 * main method used to launch program
	 * creates the scramble object
	 * provides the overall execution time
	 * @param args 0: <dictionary.txt Path>
	 * @param args 1: <matrix width>
	 * @param args 2: <matrix height> 
	 * @param args 3: OPTIONAL: <matrix.txt Path>
	 * @param args 4: OPTIONAL: <-print option>
	 */
	public static void main(String[] args) {
		//store the start time for later
		Long start = System.currentTimeMillis();
		
		//initialize the scramble object
		Scramble sc = null;
		
		//confirm that the an appropriate number of arguments were present
		if(args.length < 3) {
			printUsage();
		}
		
		//check if operator wants the words listed
		if(args[args.length-1].equals("-print")) {
			Scramble.print_words = true;
			
			//deal with 4 arguments
			// this is the case creating a randomly generated matrix
			if(args.length == 4) {
				try {
					sc = new Scramble(Integer.parseInt(args[1]), Integer.parseInt(args[2]), args[0]);
				} catch (NumberFormatException nfe) {
					System.out.println("ERROR: Invalid number entered...\n");
					printUsage();
				}
			//deal with 4 arguments
			// this is the case when reading a matrix in from a text file
			} else if (args.length == 5) {
				try{
					sc = new Scramble(Integer.parseInt(args[1]), Integer.parseInt(args[2]), args[0], args[3]);
				} catch (NumberFormatException nfe) {
					System.out.println("ERROR: Invalid number entered...\n");
					printUsage();
				}
			}
		} else {
			//deal with 3 arguments
			// this is the case creating a randomly generated matrix
			if (args.length==3) {
				//version that generates random x,y array
				try {
					sc = new Scramble(Integer.parseInt(args[1]), Integer.parseInt(args[2]), args[0]);
				} catch (NumberFormatException nfe) {
					System.out.println("ERROR: Invalid number entered...\n");
					printUsage();
				}
			//deal with 4 arguments
			// this is the case when reading a matrix in from a text file
			} else if(args.length == 4) {
				//version that reads in the the random array
				try{
					sc = new Scramble(Integer.parseInt(args[1]), Integer.parseInt(args[2]), args[0], args[3]);
				} catch (NumberFormatException nfe) {
					System.out.println("ERROR: Invalid number entered...\n");
					printUsage();
				}
			}
		}
		
		//provide completion information to the operator
		System.out.println("Total Runtime: " + (System.currentTimeMillis() - start));
		System.out.println("Words Found: " + sc.validwords.size());
	}
	
	/**
	 * Used to print usage information and exit, when invalid arguments are passed in
	 */
	private static void printUsage() {
		System.out.println("Usage: ");
		System.out.println("\tTo generate a random array use:");
		System.out.println("\t\t[dictionary.txt] [width_int] [height_int]");
		System.out.println("\tTo read in an array from a txt file (you must still specify size):");
		System.out.println("\t\t[dictionary.txt] [width_int] [height_int] [matrix.txt]");
		System.out.println("\n\tYou can also add the \"-print\" option at the end, to provide");
		System.out.println("\t a printout of the character array, and the words that were found.");
		System.exit(1);
	}
	
	/**
	 * Constructor used to build matrix from a file
	 * @param sizex width of the matrix
	 * @param sizey height of the matrix
	 * @param dictPath path to the dictionary.txt file
	 * @param matrixPath path to the matrix.txt file
	 */
	public Scramble (int sizex, int sizey, String dictPath, String matrixPath) {
		//generate matrix from file
		matrix = generateMatrix(sizex, sizey, matrixPath);
		
		if(Scramble.print_words) {
			printMatrix();
		}
		
		//pass to helper method
		process(dictPath);
	}
	
	/**
	 * Helper method called after constructor builds game board (matrix)
	 * @param dictPath path to dictionary file
	 */
	private void process(String dictPath) {
		//save the time before building library
		long st = System.currentTimeMillis();
		
		//create new trie, and fill it
		words = new Trie();
		generateTrie(dictPath, 3, matrix.length*matrix[0].length);
		
		//Print out time used to build trie for operator.
		System.out.println("Built Trie Structure: " + (System.currentTimeMillis() - st));
		
		//save the time before searching for valid words 
		long st2 = System.currentTimeMillis();
		
		//create LinkedList for words, then perform search
		validwords = new LinkedList<String>();
		getAllValidWords();
		
		//Print time to complete algorithm for user
		System.out.println("Search Algorithm time: " + (System.currentTimeMillis() - st2));
		
		if(Scramble.print_words) {
			for(String x : validwords) {
				System.out.println(x);
			}
		}
		
	}
	
	/**
	 * Constructor to run program with random x and y, and dictionary file
	 * @param sizex size in x dimension
	 * @param sizey size in y dimension
	 * @param dictPath path to dictionary file
	 */
	public Scramble (int sizex, int sizey, String dictPath) {
		//store time before beginning random generating
		long st = System.currentTimeMillis();
		
		//generate random matrix of characters
		matrix = generateMatrix(sizex, sizey);
		
		if(Scramble.print_words) {
			printMatrix();
		}
		
		//output time to generate matrix for user
		System.out.println("Matrix Generated: " + (System.currentTimeMillis()-st));
		
		//pass to helper method
		process(dictPath);
	}
	
	/**
	 * method that manages use of WordSearch thread objects
	 *  when actually searching for valid words.
	 */
	private void getAllValidWords() {
		//if matrix.length is less than the MAX possible threads,
		// create a thread for each row
		if (matrix.length <= MAX_THREADS) {
			
			//create array to store threads
			WordSearch searchers[] = new WordSearch[matrix.length];
			
			//create and start all needed threads
			for(int i = 0; i < matrix.length; i++) {
				searchers[i] = new WordSearch(i, this);
				searchers[i].start();
			}
		
			//wait for threads to finish processing before continuing.
			for(int i = 0; i < matrix.length; i++) {
				try {
					searchers[i].join();
				} catch (InterruptedException e) {
					//this should not occur
					e.printStackTrace();
				}
			}
			
		//if matrix.length is greater than MAX thread number
	    // proceed with search using only the maximum number of processes at any given time
		} else {
			
			//create matix storing maximum number of threads
			WordSearch searchers[] = new WordSearch[MAX_THREADS];
			
			//variable to track current row in matrix
			int mrowcount = 0;
			
			//variable to track current index in searchers array
			int index = 0;
			
			//while we still haven't processed each row, 
			// start a new thread when an old one finishes
			while(mrowcount < matrix.length) {
				
				//if thread is not yet created,
				//create it and increment required counters
				if(searchers[index] == null) {
					searchers[index] = new WordSearch(mrowcount, this);
					searchers[index].start();
					mrowcount++;
					index++;
					
				//if thread exists, wait for it finish, 
				// then replace it with a new thread, for the next row
				// increment the necessary counter variables
				} else {
					
					try {
						searchers[index].join();
					} catch (InterruptedException e) {
						//this should not occur
						e.printStackTrace();
					}
					
					searchers[index] = new WordSearch(mrowcount, this);
					searchers[index].start();
					mrowcount++;
					index++;
				}
				
				//if reached the end of the searchers array,
				// reset index to 0
				if(index >= searchers.length) {
					index = 0;
				}
			}
			
			//having started a thread for each row, wait for all threads to finish
			for(int i = 0; i < searchers.length; i++) {
				try {
					searchers[index].join();
				} catch (InterruptedException e) {
					//this should not occur
					e.printStackTrace();
				}
			}
		}
		//search complete
	}
	
	/**
	 * generates matrix with random characters
	 * @param sizex width of matrix
	 * @param sizey height of matrix
	 * @return character array (game board)
	 */
	private char[][] generateMatrix(int sizex, int sizey) {
		//character array that becomes matrix
		char[][] ret = new char[sizex][sizey];
		
		//generator is used to get a number between 0 and 25
		// add 65 and typecast to char to get a random uppercase character
		Random generator = new Random();
		
		for(int i = 0; i < ret.length; i++){
			for(int j = 0; j < ret[i].length; j++) {
				ret[i][j] = (char)(generator.nextInt(26)+65);
			}
		}
		
		return ret;
	}
	
	/**
	 * generate an array of sizex by sizey from the file provided
	 * @param sizex width of matrix
	 * @param sizey height of matrix
	 * @param matPath path to matrix file
	 * @return character array (game board)
	 */
	private char[][] generateMatrix(int sizex, int sizey, String matPath) {
		char[][] ret = new char[sizex][sizey];
		
		Scanner sc = null;
		try {
			sc = new Scanner(new File(matPath));
			
			int row = 0;
			while (sc.hasNextLine()) {
				String line = sc.nextLine().toUpperCase();
				String[] tokens = line.split(" ");
				for(int i = 0; i < ret.length; i++) {
					ret[row][i] = tokens[i].charAt(0);
				}
				row++;
				if(row > ret[0].length) {
					System.out.println("ERROR READING FROM FILE");
					printUsage();
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println("matrix.txt file path invalid");
			printUsage(); //will kill program
		}
		
		return ret;
	}
	
	/**
	 * Builds Trie Structure from dictionary file
	 * filtering words that are too small or too big
	 * @param dictPath path to dictionary.txt
	 * @param minSize currently 3, changes before compile
	 * @param maxSize determined based on matrix size
	 */
	private void generateTrie(String dictPath, int minSize, int maxSize) {
		try {
			Scanner sc = new Scanner(new File(dictPath));
			while(sc.hasNextLine()){
				String line = sc.nextLine();
				if (line.length() >= minSize && line.length() <= maxSize){
					words.addWord(line.toUpperCase());
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println("Invalid Dictionary filePath entered");
			printUsage();
		}
	}
	
	/**
	 * Method used to print the matrix, when print_words flag is set
	 */
	private void printMatrix() {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				System.out.print(" " + matrix[i][j]);
			}
			System.out.println();
		}
	}
	
	/**
	 * Synchronized method allows all valid words to be added safely to the validwords object
	 * @param words
	 */
	public synchronized void addAllValidWords(LinkedList<String> words) {
		validwords.addAll(words);
	}
}


