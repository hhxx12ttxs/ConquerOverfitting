package com.timpanix.yatzy;

import java.io.*;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;

public class Yatzy implements Pausable{

	// static final variables to display the faces of the dice
	private static final String DICE_NUMS = "     1           2           3           4           5"; 
	private static final String TOPLINE   = " _________   _________   _________   _________   _________";
	private static final String BOTTOMLINE = "|_________| |_________| |_________| |_________| |_________|";
	private static final String TWO_DOTS =      " o   o ";
	private static final String ONE_DOT_MIDDLE = "   o   ";
	private static final String ONE_DOT_LEFT =   " o     ";
	private static final String ONE_DOT_RIGHT =  "     o ";
	private static final String NO_DOT =        "       ";
	// the following three static final String arrays represent the 6 possible faces of the dice:
	                                            // 1			  2               3               4         5               6
	private static final String[] FACES_TOP    = {NO_DOT,  	      ONE_DOT_MIDDLE, ONE_DOT_LEFT,   TWO_DOTS, TWO_DOTS,       TWO_DOTS};
	private static final String[] FACES_MIDDLE = {ONE_DOT_MIDDLE, NO_DOT,         ONE_DOT_MIDDLE, NO_DOT,   ONE_DOT_MIDDLE, TWO_DOTS};
	private static final String[] FACES_BOTTOM = {NO_DOT,         ONE_DOT_MIDDLE, ONE_DOT_RIGHT,  TWO_DOTS, TWO_DOTS,       TWO_DOTS};
	
	private static final int NUM_OF_DICE = 5;
	private static final int NUM_OF_FACES = 6;
	private static final int NAME_MAXLENGTH = 10;
	private static final int NUM_OF_FIELDS = 16;	// number of fields on the yatzy card
	private static final int NUM_OF_ROUNDS = 13;
	private static final int MAX_ROLLS = 3;
	private static final int BONUS_UPPERSECTION = 35;
	private static final int FULL_HOUSE_SCORE = 25;
	private static final int SMALL_STRAIGHT_SCORE = 30;
	private static final int LARGE_STRAIGHT_SCORE = 40;
	private static final int YATZY_SCORE = 50;
	private static final int ADDITIONAL_YATZY_BONUS = 100;
	private static final String[] FIELD_NAMES = {"Aces", "Twos", "Threes", "Fours", "Fives", "Sixes", "    BONUS", "3 of a kind", "4 of a kind", "Full House", 
		"Small Straight", "Large Straight", "YATZY", "Chance", " Y+ BONUS", "T O T A L S"};
	private static final String[] ABBREVIATIONS = {"1", "2", "3", "4", "5", "6", "", "3k", "4k", "fh", "ss", "ls", "y", "c"};
	// players are static, so some of the information (names) can be retained
	private static Player[] players;
	private static int numOfPlayers = 0;
	private int[] currentDiceValues;
	private int[] currentDiceValueCounters;	// this is used to count the identical dice faces (eg. currentDiceValueCounters[0] = 2 means that 2 dice were rolled with face 1)
	private boolean[] diceRollStatus;	// this will be used to record which dice will be rolled
	private boolean[] validOptions;		// this will be used to record the valid options for a player after the dice were rolled
	
	
	// constructor
	public Yatzy(){
		
		currentDiceValues = new int[getNumOfDice()];
		diceRollStatus = new boolean[getNumOfDice()];
		currentDiceValueCounters = new int[getNumOfFaces()];
		validOptions = new boolean[getNumOfFields()];
	}
	
	

	
	/*-------------------------------------------------------------------------------------------------------
	 * 							METHODS FOR DICE ROLLING OPERATIONS
	 * ------------------------------------------------------------------------------------------------------
	 */
	
	
	/**
	 * this method displays the 5 dices according to the int[] provided as argument
	 */
	public void displayDice(int[] diceValues){

		// this labels the dice from 1 to 5
		System.out.println(DICE_NUMS);
		// this prints the top line of the five dice
		System.out.println(TOPLINE);
		// this prints the top third of the face (eg. 2 dots if a 6 was rolled) of all 5 dice
		System.out.println("| " + FACES_TOP[diceValues[0]] + " | | " + FACES_TOP[diceValues[1]] + " | | " + FACES_TOP[diceValues[2]] + " | | "
						        + FACES_TOP[diceValues[3]] + " | | " + FACES_TOP[diceValues[4]] + " |");
		// this prints the middle third of the face (eg. 1 dot if a 5 was rolled) of all 5 dice
		System.out.println("| " + FACES_MIDDLE[diceValues[0]] + " | | " + FACES_MIDDLE[diceValues[1]] + " | | " + FACES_MIDDLE[diceValues[2]] + " | | "
		                        + FACES_MIDDLE[diceValues[3]] + " | | " + FACES_MIDDLE[diceValues[4]] + " |");
		// this prints the bottom third of the face (eg. 1 dot in the right if a 3 was rolled) of all 5 dice
		System.out.println("| " + FACES_BOTTOM[diceValues[0]] + " | | " + FACES_BOTTOM[diceValues[1]] + " | | " + FACES_BOTTOM[diceValues[2]] + " | | "
								+ FACES_BOTTOM[diceValues[3]] + " | | " + FACES_BOTTOM[diceValues[4]] + " |");
		// this prints the bottom line of the five dice
		System.out.println(BOTTOMLINE);
	}
	
	/**
	 * this method is in charge of everything in connection with rolling the dice:
	 * - it calls the method to roll the dice
	 * - it calls the methods to set and display the valid options for the particular player
	 * - it calls the method to ask the player if he/she wants to roll again
	 * 
	 * @return: true if no options are available, false if there ARE still options available
	 */
	public boolean rollDice(Scanner sc, int playerIndex) {
		
		boolean[] rollAll = {true, true, true, true, true};
		int flag = 0;
		int rollCounter = 0;
		boolean validDiceInput = false;
		boolean noOptionsAvailable = true;
		
		
		// 1st roll: roll all dice
		rollDiceOnce(rollAll);
		rollCounter++;		// increment counter
		setValidOptions(playerIndex);
		
		noOptionsAvailable = displayOptionsForConsideration();	// display current valid options (eg. small straight, 3 of a kind)
		
		while(rollCounter < getMaxRolls()){	// loops max. 2 times
			// ask the player if he/she wants to roll again
			switch (wantToRollAgain(sc)) {
			case 1:		// player decides NOT to roll again
				rollCounter = 3;
				break;	
			case 2:			// player decides to roll ALL dice again
				rollDiceOnce(rollAll);	// roll the dice
				rollCounter++;
				setValidOptions(playerIndex);	// establish all the valid options and
				noOptionsAvailable = displayOptionsForConsideration();	// display them
				break;
			case 3:			// player decides to roll SOME dice again
				validDiceInput = false;
				while(!validDiceInput){	// loop until valid input
					flag = rollWhichDiceAgain(sc);	// roll the specified dice again
					if(flag == 1)	// valid input	// if the dice specified were valid,
						validDiceInput = true;		// set the boolean flag to true to break the while loop
					else if(flag == 0 || flag > 5){	// no input, invalid input (eg. a letter) or too much input was provided
						System.out.println("Invalid input. You asked for " + flag + " dice to be rolled again.");
					}
					else if(flag == -1)				// an int was input but not within the range specified (eg. dice no. 7)
						System.out.println("Invalid input. There are 5 dice to chose from.");
				}
				// roll dice again
				rollDiceOnce(getDiceStatus());
				rollCounter++;
				setValidOptions(playerIndex);
				noOptionsAvailable = displayOptionsForConsideration();
				break;
			}	
		}
		return noOptionsAvailable;
	}
	
	
	/**
	 * this method organises one roll of all 5 dice. It also records how many times each face was rolled. 
	 * 1. all face counter are set to 0.
	 * 2. all the dice are rolled by calling the rollOneDice() method and the values are stored in the currentDiceValues.
	 * 3. the face counters are calculated
	 * 4. the dice are displayed by calling the displayDice() method
	 */
	public void rollDiceOnce(boolean[] rollTheseDice){
		
		// reset Counters to 0
		for(int i = 0; i < getNumOfFaces(); i++)
			setCurrentDiceValueCounter(i, 0);
		
		// roll the dice
		for(int i = 0; i < getNumOfDice(); i++)
			if(rollTheseDice[i])		// if the player decided to roll this dice
				setCurrentDiceValue(i, rollOneDice()); // roll dice and store value in currentDiceValues (otherwise the old value for the dice will be kept)	
		
		// calculate updated counters:
		for(int i = 0; i < getNumOfDice(); i++)
			increaseCurrentDiceValueCounter(getCurrentDiceValues()[i]);	// increase the counter (eg. if a 3 was rolled : currentDiceValuesCounter[3]++  )	
		
		// display dice
		displayDice(getCurrentDiceValues());
	}
	
	
	/**
	 * this simple method increments one element of the currentDiceValueCounters array. The element is specified by the parameter (index)
	 */
	private void increaseCurrentDiceValueCounter(int index) {
		this.currentDiceValueCounters[index]++;
	}
	
	
	/**
	 * this method generates a pseudo random number in the range 0 - 5
	 * (will be used to represent dice values 1 - 6)
	 */
	public int rollOneDice(){
		
		Random r = new Random();
		return (r.nextInt(6));	// r.next(6) generates pseudo random values from 0 - 5
	}
	
	/**
	 * this method asks the player if he/she wants to roll the dice again and returns the user input.
	 */
	public int wantToRollAgain(Scanner sc){
		
		int option = -1;
		boolean isOk = false;
		System.out.println("\nWould you like to select one of your current options?");
		System.out.println("  Yes ................................. 1");
		System.out.println("  No, roll ALL dice again, please. .... 2");
		System.out.println("  No, roll SOME dice again, please. ... 3");
		System.out.print("Your selection: ");

		while(!isOk){
			try{
				option = sc.nextInt();
				while(option < 1 || option > 3){
					System.out.print("Invalid input. Please try again: ");
					option = sc.nextInt();
				}
				isOk = true;
			}catch(InputMismatchException e){
				System.out.print("Invalid input. Please input 1, 2 or 3: ");
				sc.next();
			}
		}
		displayLine();
		return option;
	}
	
	
	/**
	 * this method is called when the player decides to roll some dice again, but not all
	 * 
	 * @return: 1 if valid input was provided, the length of the user input if invalid input was provided
	 */
	public int rollWhichDiceAgain(Scanner sc){
		
		int temp = 0;
		int counter = 0;
		String input = new String();
		
		// reset all the values in the boolean control array to false every time this method is called
		for(int i = 0; i < getNumOfDice(); i++){
			setDiceRollStatus(i, false);
			//diceRollStatus[i] = false;
		}
		
		System.out.println("Please select the dice you would like to roll again.");
		System.out.println("(Format: no spaces, please!) eg. 1,2,3 or 123 or 1-2-3 etc.");
		System.out.print("Your selection: ");
		
		// read in the numbers of the dice that the player wants to roll again
		input = sc.next();
		System.out.println("input: " + input);
		String[] numbers = {"1", "2", "3", "4", "5"};
		for(int i = 0; i < input.length(); i++){
			for(int j  = 0; j < numbers.length; j++){
				// if the substring of the user input is equal to one or more of the 5 dice numbers (= valid input) ...
				if(input.substring(i, i + 1).equals(numbers[j])){
					temp = Integer.parseInt(numbers[j]) - 1;	//... convert the number from String to int
					setDiceRollStatus(temp, true);				// and set the boolean flag to true -> this indicates that this dice will be rolled again
					//diceRollStatus[temp] = true;				
					counter++;
				}
			}	
		}
		// if more than 5 numbers, no input or invalid input was provided
		if(counter > 5 || counter == 0)
			return counter;	// indicates invalid input
		
		return 1;	// indicates everything went well
	}
	
	
	/**
	 * this method calculates the sum of all current dice faces
	 */
	public int sumOfAllDiceValues(){
		
		int sum = 0;
		for(int i = 0; i < getCurrentDiceValues().length; i++){
			sum += getCurrentDiceValues()[i] + 1;	// + 1 because the dice values are stored as 0,1,2,3,4,5
		}
		return sum;
	}
	
	
	/*-------------------------------------------------------------------------------------------------------
	 * 									SIMPLE DISPLAY METHODS
	 * ------------------------------------------------------------------------------------------------------
	 */
	
	
	/**
	 * this method displays the title of the game (two dice with the word "YATZY" between them)
	 */
	public void displayTitle(){
		
		displayDoubleLine();
		System.out.println(" _________                _______  _____             _________ ");
		System.out.println("|  o   o  |   \\    /  /\\     |         /  \\    /    |  o      |");
		System.out.println("|  o   o  |    \\  /  /  \\    |      __/_   \\  /     |    o    |");
		System.out.println("|  o   o  |     \\/  /____\\   |      /       \\/      |      o  |");
		System.out.println("|_________|     |  /      \\  |     /____     |      |_________|\n");	
		System.out.println("\t       Created by Daniel Bertschi, 2013");
		displayDoubleLine();
		System.out.println("");

	}
	
	
	/**
	 * this method displays a double separating line
	 */
	public void displayDoubleLine() {
		System.out.println("===============================================================");
	}
	
	/**
	 * this method displays a single separating line
	 */
	public void displayLine() {
		System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
	}
	
	/**
	 * this method displays a double line on the game card (the length depends on the amount of players involved)
	 */
	private void displayDoubleDivider() {
		System.out.print("================");
		for(int i = 0; i < getNumOfPlayers(); i++)
			System.out.print("================");
		System.out.println("");
		
	}

	/**
	 * this method displays a single line on the game card (the length depends on the amount of players involved)
	 */
	private void displayDivider() {
		System.out.print("----------------");
		for(int i = 0; i < getNumOfPlayers(); i++)
			System.out.print("----------------");
		System.out.println("");
		
	}
	
	
	/**
	 * this method displays a simple version of a progress bar (used during saving and restoring of a game)
	 */
	public void displayProgressBar(){
		try {
			for(int i = 0; i < 20; i++){
				Thread.sleep(50);		// pause for 50 ms
				System.out.print("..");	// and display 2 dots
			}
			System.out.println("");
		} catch (InterruptedException ie) {
				ie.printStackTrace();
		}
	}
	
	
	
	/*-------------------------------------------------------------------------------------------------------
	 * 							SIMPLE GETTER METHODS FOR STATIC FINAL VARIABLES (CONSTANTS)
	 * ------------------------------------------------------------------------------------------------------
	 */
	
	public static String[] getAbbreviations(){
		return ABBREVIATIONS;
	}
	
	public static String[] getFieldNames(){
		return FIELD_NAMES;
	}
	
	public static int getNumOfRounds(){
		return NUM_OF_ROUNDS;
	}
	
	public static int getMaxRolls(){
		return MAX_ROLLS;
	}
	
	public static int getNumOfFields(){
		return NUM_OF_FIELDS;
	}
	
	public static int getNumOfDice(){
		return NUM_OF_DICE;
	}
	
	public static int getNumOfFaces(){
		return NUM_OF_FACES;
	}
	
	private static int getNameMaxLength() {
		return NAME_MAXLENGTH;
	}
	
	private static int getBonusUpperSection(){
		return BONUS_UPPERSECTION;
	}
	
	private static int getFullHouseScore(){
		return FULL_HOUSE_SCORE;
	}
	
	private static int getSmallStraightScore(){
		return SMALL_STRAIGHT_SCORE;
	}
	
	private static int getLargeStraightScore(){
		return LARGE_STRAIGHT_SCORE;
	}
	
	private static int getYatzyScore(){
		return YATZY_SCORE;
	}
	
	private static int getAdditionalYatzyBonus(){
		return ADDITIONAL_YATZY_BONUS;
	}
	
	
	/*-------------------------------------------------------------------------------------------------------
	 * 						SIMPLE GETTER AND SETTER METHODS FOR STATIC AND INSTANCE VARIABLES
	 * ------------------------------------------------------------------------------------------------------
	 */
	
	public static int getNumOfPlayers(){
		return numOfPlayers;
	}
	
	public static Player[] getPlayers(){
		return players;
	}
	
	public boolean[] getDiceStatus(){
		return this.diceRollStatus;
	}
	public boolean[] getValidOptions(){
		return this.validOptions;
	}
		
	public int[] getCurrentDiceValues(){
		return this.currentDiceValues;
	}
	
	public int[] getCurrentDiceValueCounters(){
		return this.currentDiceValueCounters;
	}
	
	private void setCurrentDiceValueCounter(int index, int value) {
		this.currentDiceValueCounters[index] = value;
	}

	public void setDiceRollStatus(int diceIndex, boolean value){
		this.diceRollStatus[diceIndex] = value;
	}
	
	private void setCurrentDiceValue(int index, int value){
		this.currentDiceValues[index] = value;
	}


	
	/*-------------------------------------------------------------------------------------------------------
	 * 								METHODS FOR PLAYER OPERATIONS
	 * ------------------------------------------------------------------------------------------------------
	 */
	
	
	/**
	 * this method asks for the player's names and checks that they don't exceed the allowed max length
	 * and that they are unique
	 */
	public void setPlayers(Scanner sc) {
		
		boolean[] playersAreOk = new boolean[getNumOfPlayers()];
		boolean isDuplicateName;
		int displayNumber = 0;
		
		for(int i = 0; i < getNumOfPlayers(); i++){
			displayNumber = i + 1;
			System.out.print("Name of Player " + displayNumber + ": ");
			while(!playersAreOk[i]){
				isDuplicateName = false;
				String name = sc.next();		// get input from the player
				if(name.length() <= getNameMaxLength()){		// check that the input is not longer than 10 chars
					// check that the input is unique
					for(int j = 0; j < getNumOfPlayers(); j++){
						if(name.equals(getPlayers()[j].getName()))	// if the name has been used before
							isDuplicateName = true;		// set boolean value to true
					}
					if(isDuplicateName)
						System.out.print("This name was chosen already, please chose a unique name: ");
					else{	
						getPlayers()[i].setName(name);
						playersAreOk[i] = true;
					}
				}else
					System.out.print("Name is too long (max. 10 letters). Please use a shorter name: ");
			}
		}
	}

	/**
	 * this method asks for the amount of players involved in the game. It checks that the number of players is in the allowed
	 * range (1-4 players) and that an actual number is input by the user/player
	 */
	public void setNumOfPlayers(Scanner sc) {

		boolean isOk = false;
		int temp = 0;
		
		System.out.print("How many players are involved in this game? ");
		while(!isOk){
			try{
				temp = sc.nextInt();	// try to store the input value in temp
				if(temp > 0 && temp < 5){	// check that it is a value from 1 - 4
					numOfPlayers = temp;	// if yes: store it in the proper variable
					isOk = true;			// and set the flag to true
				}else			// if it was an int but not between 1 and 4, print out error message
					System.out.print("Invalid input. Number of players allowed: 1 - 4. Please try again: ");
			}catch(InputMismatchException e){	// if it wasn't a number: print out error message
				System.out.print("Invalid input. Please try again: ");
				sc.next();
			}
		}
		
		// instantiate array of class Player
		players = new Player[getNumOfPlayers()];
		for(int i = 0; i < getNumOfPlayers(); i++)
			players[i] = new Player();	///// IMPORTANT: initialise every single element of the array!!! ///// 
	}
	
	/**
	 * this method creates a random number in the range of 0 - the number of players involved in the game. 
	 * The player with the randomly selected number as index in the players array will start the game.
	 * 
	 * @return: the randomly generated number (int)
	 */
	public int selectRandomPlayer() {
		
		Random r = new Random();
		int i = r.nextInt(getNumOfPlayers());
		return i;
	}
	
	
	/**
	 * this method asks the players who would like to make the first move. The players can either input the name of a player
	 * or chose random selection.
	 * 
	 * @return: the index of the starting player (int)
	 */
	public int whoStartsTheGame(Scanner sc) {
		
		String starter = new String();
		int starterIndex = -1;
		displayLine();
		System.out.println("Who makes the first move?");
		System.out.println("Please type \n - the starting player's name OR\n - select \"R\" for random selection");
		
		boolean selectionOk = false;
		while(!selectionOk){
			System.out.print("Your selection: ");
			starter = sc.next();
			if(starter.toUpperCase().equals("R")){		// random selection was chosen
				starterIndex = selectRandomPlayer();
				selectionOk = true;
			}else{
				for(int i = 0; i < getNumOfPlayers(); i++){
					if(starter.equals(getPlayers()[i].getName())){
						starterIndex = i;
						selectionOk = true;
						break;	// break loop immediately
					}
					if((i == getNumOfPlayers() - 1) && !selectionOk){// invalid input
						System.out.println("This is not a valid selection. Please try again.");
					}
				}
			}
		}

		displayDoubleLine();
		System.out.println(getPlayers()[starterIndex].getName() + " starts the game."); 
		
		return starterIndex;
	}
	
	/**
	 * this method returns the next player of the game
	 */
	public int getNextPlayer(int currentPlayerIndex) {
		
		// if the currentPlayer is the last one in the array, the next player will be the first one in the array (return 0)
		// otherwise the next player is the one after the current player
		return (currentPlayerIndex == getNumOfPlayers() - 1) ? 0 : currentPlayerIndex + 1;
	}
	
	
	/*-------------------------------------------------------------------------------------------------------
	 * 								METHODS FOR YATZY OPERATIONS
	 * ------------------------------------------------------------------------------------------------------
	 */
	
	/*
	 * this method is in charge of the whole process involved in selecting a field from the currently valid options 
	 * (depending on the currently rolled dice).
	 * 1. it checks that the user inputs a valid option (a field abbreviation displayed for selection).
	 * 2. it defines for every field on the card, how the points are calculated.
	 * 3. it updates the player's game card accordingly.
	 * 
	 * Additional checks:
	 * - every time a field from the upper section is selection, it checks if the upper section bonus applies
	 * 	 (if the sum of all upper section scores is at least 63)
	 * - if an additional yatzy bonus applies, it is also updated on the player's card
	 */
	public void makeSelection(Scanner sc, int playerIndex) {
		
		boolean isOk = false;
		int points = 0;
		
		displayOptionsForSelection();
		System.out.print("Your selection: ");
		String input = sc.next();
		
		// make sure that the input represents a valid option
		Outerloop: while(!isOk){
			for(int i = 0; i < getNumOfFields() - 2; i++){
				// if the input is a correct abbreviation and was one of the abbreviations that were displayed (= a valid option)
				if(input.equals(getAbbreviations()[i]) && getValidOptions()[i] == true){
					isOk = true;
					// register the selection and update the player's card
					switch(i){
					case 0:		// aces
					case 1:		// twos
					case 2:		// threes
					case 3:		// fours
					case 4:		// fives
					case 5:		// sixes
						// the following applies to all the above cases:
									// value of face * number of dice with that face
						points = getCurrentDiceValueCounters()[i] * (i + 1);  // eg. 4 dice were rolled with face 5 : 4 * 5 = 20 points
						// update the player's gamecard
						getPlayers()[playerIndex].updateGameCard(i, points);
						// if the player hasn't won the bonus for the upper section yet, check again
						if(getPlayers()[playerIndex].getGameCard(6) == -1)
							checkForUpperBonus(playerIndex);
						break;
					case 7:		// player selected 3 of a kind
					case 8:		// player selected 4 of a kind
					case 13:	// player selected chance
						getPlayers()[playerIndex].updateGameCard(i, sumOfAllDiceValues());	// this applies to 3 of a kind, 4 of a kind and chance
						break;
					case 9:		// player selected Full House
						getPlayers()[playerIndex].updateGameCard(i, getFullHouseScore());
						break;
					case 10:		// player selected Small Straight
						getPlayers()[playerIndex].updateGameCard(i, getSmallStraightScore());
						break;
					case 11:	// player selected Large Straight
						getPlayers()[playerIndex].updateGameCard(i, getLargeStraightScore());
						break;
					case 12:	// player selected Yatzy
						getPlayers()[playerIndex].updateGameCard(i, getYatzyScore());
						break;	
					}
					break Outerloop;
				}
			}
			System.out.print("This is not an option. Please try again: ");
			input = sc.next();
		}
		// if an additional yatzy bonus applies, write it "to card"
		if(getValidOptions()[14] == true)
			getPlayers()[playerIndex].updateGameCard(14, getAdditionalYatzyBonus());
		displayDoubleLine();
	}
	
	/**
	 * this method checks if the upper section bonus applies (if the sum of all upper section scores is at least 63).
	 * If this is the case, a congratulatory message is displayed and the player's game card is updated accordingly.
	 */
	private void checkForUpperBonus(int playerIndex) {
		
		int sum = 0;
		for(int i = 0; i < 6; i++)
			sum += getPlayers()[playerIndex].getGameCard(i);
		if(sum >= 63){
			System.out.println("Congratulations! You just won a bonus of 35 points\n" +
					"because you collected at least 63 points in the upper section");
			getPlayers()[playerIndex].updateGameCard(6, getBonusUpperSection());
		}
		
	}

	/**
	 * this method displays the complete game card (summary of all fields for all the players).
	 * Scores are shown as follows: 
	 * - not yet selected fields with a hyphon
	 * - played fields with the actual score
	 * - fields which were set empty (because no option was available) are indicated as 0.
	 * 
	 * This is an example of the play card for 2 players after 1 round.
	 * 					player 1		player 2
	 *	------------------------------------------------
	 *	Aces			-				-	
	 *  Twos			6				-	
	 *  Threes			-				3	
	 *  Fours			-				-	
	 *  Fives			-				-	
	 *  Sixes			-				-	
	 * 	------------------------------------------------
     *     BONUS		-				-	
     *  ------------------------------------------------
	 *  3 of a kind		-				-	
	 *  4 of a kind		-				-	
	 *  Full House		-				-	
	 *  Small Straight	-				-	
	 *  Large Straight	-				-	
	 *  YATZY			-				-	
	 *  Chance			-				-	
	 *  ------------------------------------------------
 	 *     Y+ BONUS		-				-	
 	 *  ------------------------------------------------
 	 *  T O T A L S		6				3	
 	 *  ================================================
	 */
	public void displayCard(){
		
		// display the player's names
		for(int i = 0; i < getNumOfPlayers(); i++)
			System.out.print("\t\t" + getPlayers()[i].getName());
		System.out.println("");	// new line
		displayDivider();	// displays a dividing line
		// display the field name (eg. "Aces")
		for(int i = 0; i < getNumOfFields(); i++){
			System.out.print(getFieldNames()[i]);	
			if(i < 6 || i == 12 || i == 13)		// if the field name is short, 
				System.out.print("\t");			// add an extra tab to get an even looking display
			// display stored values ...
			for(int j = 0; j < getNumOfPlayers(); j++){
				if(getPlayers()[j].getGameCard()[i] != -1)	// ... if they're not equal to -1
					System.out.print("\t" + getPlayers()[j].getGameCard()[i] + "\t");
				else
					System.out.print("\t-\t");				// otherwise just display a hyphon
			}
			System.out.println(""); // new line
			if(i == 5 || i == 6 || i == 13 || i == 14)	// display a horizontal line before and after the Bonus fields
				displayDivider();
			if(i == 15)		// display double horizontal line after the totals field
				displayDoubleDivider();
			
		}
	}
	

	/**
	 * this method is called, when the player decides to select one of the currently available valid options.
	 * If an additional yatzy bonus applies, a congratulatory message is displayed first.
	 * Otherwise just all the currently available options and their abbreviations are displayed. 
	 */
	public void displayOptionsForSelection(){
		
		boolean additionalYatzyBonus = false;
		
		System.out.println("\nPlease make a selection: ");
	
		if(getValidOptions()[14] == true){
			System.out.println("!! ADDITIONAL YATZY BONUS (100 Points) !!");
			System.out.println("In order to get the bonus, you need to select one of the following options: ");
			additionalYatzyBonus = true;
		}
		// loop through all fields except the 2 last ones	
		for(int i = 0; i < getNumOfFields() - 2; i++){		// -2: don't need to check for additional yatzy bonus or totals
			if(getValidOptions()[i] == true){
				System.out.println(" " + getAbbreviations()[i] + "\t" + getFieldNames()[i]);
				// if an additional yatzy bonus applies: if the appropriate upper section field has not been played yet, it MUST be selected now
				if(additionalYatzyBonus && i < 6)	// as soon as the first valid field  in the upper section is found, break the loop
					break;
			}
		}
	}
	
	
	/**
	 * this method is called everytime after the dice were rolled. It shows to the player what fields he/she could currently select.
	 * If an additional yatzy bonus applies, an information message is displayed first.
	 * If there are no valid options, an emtpy line is displayed.
	 * 
	 * @return: true if options are available and false otherwise.
	 */
	public boolean displayOptionsForConsideration(){
	
		boolean OptionsAvailable = false;
		boolean firstToDisplay = true;
		boolean additionalYatzyBonus = false;
		System.out.println("\nYour current options: ");
		
		if(getValidOptions()[14] == true){
			System.out.println(" !! ----- ADDITIONAL YATZY BONUS (100 Points) ----- !!");
			additionalYatzyBonus = true;
		}
		for(int i = 0; i < getNumOfFields() - 2; i++){		// -2: don't need to check for additional yatzy bonus or totals
			if(getValidOptions()[i] == true){
				if(!firstToDisplay)		// print a comma before every field name except the first one
					System.out.print(", ");
				System.out.print(getFieldNames()[i]);	// print field name
				OptionsAvailable = true;
				firstToDisplay = false;
				// if an additional yatzy bonus applies: if the appropriate upper section field has not been played yet, it MUST be selected now
				if(additionalYatzyBonus && i < 6)	// as soon as the first valid field  in the upper section is found, break the loop
					break;
			}
		}
		System.out.println("");	// end with a new line
		
		if(!OptionsAvailable)	// display empty line if no options are available
			System.out.println(" ---------- ");
		
		return OptionsAvailable;
	}
	
	/**
	 * this method contains all the logic in relation to what it means for a particular field to be a valid option.
	 * It uses the currentDiceValueCounters to check for every field of the game whether it is a valid option for the currently rolled dice.
	 * If a field is found to be a valid option, the corresponding element of the boolean[] validOptions is set to true.
	 */
	public void setValidOptions(int playerIndex){
		
		boolean has2 = false;
		boolean has3 = false;
		boolean additionalYatzyBonus = false;
		
		// reset the valid options to false
		for(int i = 0; i < getValidOptions().length; i++)
			validOptions[i] = false;
		// field indices: 0 "Aces", 1 "Twos", 2 "Threes", 3 "Fours", 4 "Fives", 5 "Sixes", 6 upper bonus, 7 "3 of a kind", 
		//	8 "4 of a kind", 9 "Full House", 10 "Small Straight", 11 "Large Straight", 12 "YATZY", 13 "Chance", 14 yatzy bonus,
		// 15 totals
		
		int straightCounter = 0;
		
		for(int i = 0; i < getNumOfFaces(); i++){
			// check if Yatzy was rolled	
			if(getCurrentDiceValueCounters()[i] == 5)
				if(getEmptyFields(playerIndex)[12])		// if it wasn't played before	
					validOptions[12] = true;		// Yatzy!
				// special case: 
				else if(getPlayers()[playerIndex].getGameCard()[12] != 0){	// if the yatzy field wasn't crossed out
					validOptions[14] = true;		// additional yatzy bonus !! 
					additionalYatzyBonus = true;
				}	
			// check for all fields of the upper section
			if(getCurrentDiceValueCounters()[i] >= 1){	
				if(getEmptyFields(playerIndex)[i]){	// if it hasn't been played yet
					validOptions[i]= true;	// eg. if at least one "6" was rolled, then it is a valid "sixes"
					if(additionalYatzyBonus)	// if an additional yatzy bonus applies (and the relevant upper section field is unused) ...
						return;			// don't perform the rest of the tests (because this is the only valid or feasible option)
				}
				// pre-check for large/small straight:
				straightCounter++;		// counter is incremented if subsequent faces are represented at least once
			}
			// check if 4 of a kind were rolled and if they weren't played before
			if(getCurrentDiceValueCounters()[i] >= 4 && getEmptyFields(playerIndex)[8]){
				validOptions[8] = true;		// 4 of a kind!
			}
			// check if 3 of a kind were rolled
			if(getCurrentDiceValueCounters()[i] >= 3){
				if(getEmptyFields(playerIndex)[7])	// check if it hasn't been played yet
					validOptions[7] = true;		// 3 of a kind!
					// 3 of a kind is also a condition for full house:
					has3 = true;
			}	
			// check if 2 of a kind were rolled (2nd condition for full house)
			if(getCurrentDiceValueCounters()[i] == 2)
					has2 = true;
			if(getCurrentDiceValueCounters()[i] == 0 && straightCounter < 4)
				straightCounter = 0;	// otherwise counter is reset to 0 (unless a straight was found already)
		}
		// check for full house
		if(getEmptyFields(playerIndex)[9]){	// if it hasn't been played yet
			// full House is an option if one of the following applies:
			//  - 3 of a kind and 2 of a kind were rolled  OR
			//  - an additional yatzy was rolled (allLowerAllowed)
			if((has3 && has2) || additionalYatzyBonus)
			validOptions[9] = true;		// Full House!
		
		// check for large straight
		// large straight is an option if it hasn't been used before and one of the following applies:
		// - straightCounter is 5  OR
		// - additional yatzy was rolled
		// IMPORTANT: in case a player already played large straight, we also need to show that it is a valid small straight
		}
		if(straightCounter == 5 || additionalYatzyBonus){
			if(getEmptyFields(playerIndex)[11])
				validOptions[11] = true;	// Large Straight!
			if(getEmptyFields(playerIndex)[10])
				validOptions[10] = true;		// also valid small straight
		}
		
		// check for small straight
		// small straight is an option if it hasn't been used before and one of the following applies:
		// - straightCounter is 4  OR
		// - additional yatzy was rolled
		if(getEmptyFields(playerIndex)[10])
			if(straightCounter == 4 || additionalYatzyBonus)
				validOptions[10] = true;		// Small Straight!
		
		// chance is always an option, so we only need to check whether it has been played already
		if(getEmptyFields(playerIndex)[13])
			validOptions[13] = true;
	}


	/**
	 * this method is called, when a player doesn't have a single valid option.
	 * He/she is now asked to select a field to be crossed out (set to 0).
	 * If an additional yatzy bonus applies, the player is informed, that he/she still needs to cross out
	 * one of the remaining fields in order to get the bonus.
	 * The method then checks that the input is valid.
	 */
	public void crossOutField(Scanner sc, int playerIndex) {
		
		boolean isOk = false;
		
		if(getValidOptions()[14] == true){
			System.out.println("!! ADDITIONAL YATZY BONUS (100 Points) !!");
			System.out.println("In order to get the bonus, you need to cross out an empty field.");
			getPlayers()[playerIndex].updateGameCard(14, getAdditionalYatzyBonus());
		}else
			System.out.println("Sorry, there are no options available. You need to cross out an empty field.");
		System.out.println("Please select one from the following list:");
		
		// loop through all fields except the 2 last ones and display all the remaining empty fields
		for(int i = 0; i < getNumOfFields() - 2; i++){		// -2: don't need to check for additional yatzy bonus or totals
			if(getEmptyFields(playerIndex)[i] == true){	
				System.out.println(" " + getAbbreviations()[i] + "\t" + getFieldNames()[i]);
			}
		}
		System.out.println("");	// end with a new line
		System.out.print("Your selection: ");
		// get the chosen field to be set to 0 from player
		String input = sc.next();
		
		// make sure that the input represents a valid option
		Outerloop: while(!isOk){
			for(int i = 0; i < getNumOfFields() - 2; i++){		// don't need to loop through the yatzy bonus and the totals
				// if the input is a correct abbreviation and was one of the abbreviations that were displayed (= a valid empty field)
				if(input.equals(getAbbreviations()[i]) && getEmptyFields(playerIndex)[i] == true){
					// set chosen field to 0
					getPlayers()[playerIndex].updateGameCard(i, 0);
					isOk = true;
					break Outerloop;
				}
			}
			System.out.print("This is not an option. Please try again: ");
			input = sc.next();
		}
		displayDoubleLine();
	}

	/**
	 * this method is called by the crossOutField() and the setValidOptions() methods to get an array of all the currently unused fields of a particular player.
	 * It runs through all the fields of a player's card and checks if their value is equal to -1 (this means unplayed).
	 * It does NOT check the 6th element of the array which represents the upper bonus field (the player should never be able to select this field).
	 * 
	 * @return: a boolean array. If an element is set to true, it means that this field is currently free. 
	 */
	private boolean[] getEmptyFields(int playerIndex) {
		
		boolean[] emptyFields = new boolean[getNumOfFields()];
		for(int i = 0; i < getNumOfFields(); i++){
			if( (getPlayers()[playerIndex].getGameCard(i) == -1)  &&  (i != 6))	// i = 6 represents the upper section bonus field 
				emptyFields[i] = true;											// which should not be valid even if it is empty
			else
				emptyFields[i] = false;
		}
		
		return emptyFields;
	}

	
	
	
	
	/*-------------------------------------------------------------------------------------------------------
	 * 								METHODS TO ACCESS THE YATZY RULES
	 * ------------------------------------------------------------------------------------------------------
	 */
	
	/**
	 * this method displays all the different options regarding the display of yatzy rules. The player is asked to select an option.
	 * The method loops until the player provides valid input and then calls the displayRules() method to display the selected rule.
	 */
	public void displayWhichRule(Scanner sc){
		
		displayLine();
		System.out.println("\nOptions:");
		System.out.println(" - All rules..............................1");
		System.out.println(" - Goal of the game.......................2");
		System.out.println(" - Rolling the dice.......................3");
		System.out.println(" - Scoring................................4");
		System.out.println(" - Upper section scoring..................5");
		System.out.println(" - Lower section scoring..................6");
		System.out.println(" - Additional Yatzy rules.................7");
		System.out.println(" - What to do if no options available.....8");
		System.out.print("Your selection: ");

		boolean isOk = false;
		String errorMessage = "Invalid input. Please try again:";
		int input = 0;
		while(!isOk){
			try{
				input = sc.nextInt();
				if(input >= 1 && input <= 8)
					isOk = true;
				else
					System.out.print(errorMessage);
			}catch (InputMismatchException imme){
				System.out.print(errorMessage);
				sc.next();	// IMPORTANT: this is needed for the scanner to wait for the next input
			}
		}	
		displayRules(input);
	}
	
	
	
	/**
	 * this method takes the user input from the displayWhichRule() method and calls the readFromFile() method 
	 * with the relevant startPositions and number of Lines.
	 */
	private void displayRules(int option){
		
		final int[] startPositions = {0, 0, 374, 628, 816, 1406, 2197, 3157};
		final int[] numOfLines = {76, 7, 5, 56, 12, 38, 10, 4};
		
		displayLine();
		System.out.println("");
		try{
			RandomAccessFile file = new RandomAccessFile("yatzyrules.txt", "r");
			boolean success = readFromFile(file, startPositions[option - 1], numOfLines[option - 1]);
			if(!success){
				System.out.println("There was a problem with reading data from the file");
			}
			file.close();
		}catch (IOException ioe){
			System.out.println("The file could not be found.");
		}finally{
			
		}
	}
	
	/**
	 * this method is called from the displayRules(int option) method. It reads from the specified RandomAccessFile from the
	 * indicated starting position (parameter 2) and displays the specified number of lines (parameter 3)
	 * 
	 * @return: true if everything worked, false if there was a problem
	 */
	private boolean readFromFile(RandomAccessFile file, int startPosition, int numOfLines){
		
		try{
			// move the "cursor" in the file to the specified position
			file.seek(startPosition);
			for(int i = 0; i < numOfLines; i++){	// read the specified number of lines
				System.out.println(file.readLine());	// and display them on the screen
			}
			return true;
		}catch (IOException ioe){
			return false;
		}
	}
	
	
	/*-------------------------------------------------------------------------------------------------------
	 * 								METHODS FOR HIGHSCORE OPERATIONS
	 * ------------------------------------------------------------------------------------------------------
	 */
	
	
	/**
	 * this method is called at the end of every game to update the highscores. It checks if the file is empty and can be read. 
	 * If the file can be read but is empty, the current player's score is automatically added.
	 * In the normal case, where the file is not empty, all the existing highscores are read in from the file.
	 * If there are less than 10 highscores in the file, the current one is added automatically, otherwise the current score is
	 * compared to the lowest score in the list. If it is higher than the currently lowest score, it replaces the currently lowest score.
	 * The whole list is then sorted and serialized again.
	 * 
	 * @return: true if everything worked, false if there was a problem.
	 */
	public boolean updateHighScores(int currentPlayer){
		
		final int maxNumOfElements = 10;
		
		// get today's date in its String representation
		String today = getTodaysDate();
		
		// instantiate a new Highscore object and store the current score, name of the player and today's date
		Highscore currentScore = new Highscore(getPlayers()[currentPlayer].getName(),
										  today,
				                          getPlayers()[currentPlayer].getGameCard(15));
		
		// instantiate new file object
		File scoreFile = new File("highscores.ser");
		//	check: if file doesn't exist, cannot be read or is empty, write the current score to the file 
		//  (the file will be created automatically as part of the writing operation if it doesn't exist)
		if( (!scoreFile.exists()) || (!scoreFile.canRead()) || scoreFile.length() == 0){
			List<Highscore> newList = new ArrayList<Highscore>();	// instantiate new List,
			newList.add(currentScore);								// add the current score to it
			return writeHighScoresToFile(newList, scoreFile);		// and write it to the file
		}	
		// if everything is fine and the file is not empty (normal case):
		else{
			try{
				// open stream
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(scoreFile));
				try{
					// read the whole ArrayList in from the file
					@SuppressWarnings("unchecked")
					List<Highscore> list = (ArrayList<Highscore>)in.readObject();
					// if there are less than 10 elements in the list, ...
					if(list.size() < maxNumOfElements)	
						list.add(currentScore);	// ... add the current score to the list
					// if the last element of the list is less than the score of the current player, ...
					else if(list.get(list.size() - 1).getScore() < getPlayers()[currentPlayer].getGameCard(15))
						list.set(list.size() - 1, currentScore); 	// replace the last element of the list with the current score
					
					// sort the list again
					Collections.sort(list);	// sorting is done according to the implementation of the compareTo method
					in.close();
					return writeHighScoresToFile(list, scoreFile);			// SUCCESS !!!!
				} catch (ClassNotFoundException cnfe){
					System.out.println("Update: The object could not found");
					in.close();
					return false;
				}
			}catch (IOException ioe){
				System.out.println("Update: The file " + scoreFile + " could not be opened.");
				return false;
			}
		}
	}
		
	/**
	 * this method instantiates a Date object to get "today's" date. The date is formatted according to the default configuration of the player's
	 * computer/place of residence. This method is used to generate highscore entries.
	 * 
	 * @return: String representation of "today's" date
	 */
	private String getTodaysDate() {
		
		// instantiate new Date object
		Date date = new Date();
		// retrieve information about the local date format settings
		DateFormat local = DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.getDefault());
		// format today's date according to the player's computer's default settings and return its String representation
		return local.format(date).toString();
	}

	/**
	 * this method is called by the updateHighScores() method to write a list of highscore objects to file.
	 * 
	 * @param: list of highscore objects, file
	 * @return:	true if success, false if there was  a problem
	 */
	private boolean writeHighScoresToFile(List<Highscore> list, File file) {
			
		// write the updated highscores back to the file
		try{
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
			try{
				out.writeObject(list);
				out.close();
				return true;		// SUCCESS !!!
			} catch(NotSerializableException nse){
				System.out.println("this object cannot be serialized");
				return false;
			}
		}catch(IOException ioe){
			System.out.println("File could not be opened");
			return false;
		}
	}

	/**
	 * this method reads the serialized data from the file and stores all the highscore elements in a list. 
	 * For each high score (max. 10),it displays the rank, name of the player, the date of the game and the score itself.
	 * An example display of the highscores looks like this:
	 * 				  =========================
	 *	 			  |  H I G H S C O R E S  |
	 *	 			  =========================
	 *
	 * 		Rank	Player			Date			Score
	 *		---------------------------------------------
	 * 		1		Player 1		10-Jun-2013		237
	 *		2		Player 3		10-Jun-2013		188
	 *		3		Player 4		10-Jun-2013		177
	 *		4		Player 2		11-Jun-2013		177
	 *		5		Player 1		12-Jun-2013		155
	 *		6		Player 3		10-Jun-2013		152
	 *		7		Player 7		10-Jun-2013		150
	 *		8		Player 19		11-Jun-2013		149
	 *		9		Player 5		12-Jun-2013		137
	 *		10		Player 3		11-Jun-2013		133
	 * 		---------------------------------------------
	 */
	public void displayHighScores() {

		File scoreFile = new File("highscores.ser");
		int rank = 1;
		displayLine();
		System.out.println("\n\t\t =========================");
		System.out.println("\t\t |  H I G H S C O R E S  |");
		System.out.println("\t\t =========================\n");
		System.out.println("\tRank\tPlayer\t\tDate\t\tScore");
		System.out.println("\t---------------------------------------------");
		
		// open txt file with the highscores
		try {
			ObjectInputStream stream = new ObjectInputStream(new FileInputStream(scoreFile));
			try{
				@SuppressWarnings("unchecked")
				List<Highscore> list = (ArrayList<Highscore>) stream.readObject();
				for(Highscore h : list){
					System.out.print("\t" + rank++ + "\t" + h.getName() + "\t");
					if(h.getName().length() < getNameMaxLength())
						System.out.print("\t");
					System.out.print(h.getDate() + "\t" + h.getScore() + "\n");
				}
				System.out.println("\t---------------------------------------------");

				stream.close();		//important: close stream!	
			} catch (ClassNotFoundException cnfe){
				System.out.println("The object could not found");
				stream.close();
			}
		}catch (IOException ioe){
			System.out.println("display problem! The file " + scoreFile + " could not be opened.");
		}
	}

	

	/*-------------------------------------------------------------------------------------------------------
	 * 					METHODS TO SAVE/RESTORE A GAME (INCL. IMPLEMENTED METHODS OF INTERFACE PAUSABLE)
	 * ------------------------------------------------------------------------------------------------------
	 */
	
	
	/**
	 * this is the implementation of the saveGameState() method from the interface Pausable
	 * (non-Javadoc)
	 * @see com.timpanix.yatzy.Pausable#saveGameState(com.timpanix.yatzy.Player[], int)
	 * 
	 * It copies the static players array into a new non-static array, so that it can be serialized.
	 * Then it serializes this new players array.
	 * 
	 * @return: true if everything worked, false if there was a problem
	 */
	@Override
	public boolean saveGameState(Player[] players, int round, int currentPlayer) {
		
		// copy the players into a new non-static Player[], so that it can be serialized
		Player[] SavedPlayers = players;
		
		// serialize the players array
		String filename = "pausedGame.ser";
		try{
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));
			try{
				out.write(round);
				out.write(getNextPlayer(currentPlayer));
				out.writeObject(SavedPlayers);
				out.close();
				return true;
			} catch(NotSerializableException nse){
				System.out.println("this object cannot be serialized");
			}
		}catch(IOException ioe){
			System.out.println("File could not be opened");
		}
		return false;
	}

	/**
	 * this is the implementation of the restoreGame() method from the interface Pausable
	 * (non-Javadoc)
	 * @see com.timpanix.yatzy.Pausable#restoreGame()
	 * 
	 * this method restores a saved game. It reads the following serialized data back from the file:
	 * 	- how many rounds were played before the game was saved
	 *  - which player played last
	 *  - all the player information (names, game cards etc.)
	 * After the data has been read back successfully, the content of the serial file is wiped (this avoids that the same game can be restored more than once)
	 * 
	 * @return: an int array consisting of the last round played and the last player involved if everything worked,
	 * 			otherwise an int array with values {-1,0} to indicate that there was a problem
	 */
	@Override
	public int[] restoreGame() {
		
		int[] roundAndNextPlayer = new int[2];
		int[] didntWork = {-1,0};
		Player[] restoredPlayers = new Player[getNumOfPlayers()];
		File file = new File("pausedGame.ser");
		
		if(! (file.exists() || file.canRead())){
			System.out.println("Sorry, the file " + file + " could not be found");
			return didntWork;
		}
		
		try{
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
			try{
				// read serialized data back from file
				roundAndNextPlayer[0] = (int) in.read();	// read round from file
				roundAndNextPlayer[1] = (int) in.read();	// read playerIndex from file
				restoredPlayers = (Player[]) in.readObject();	// read players (names, gamecards etc.) from file
				resetPlayers(restoredPlayers);
				in.close();
			} catch (ClassNotFoundException cnfe){
				System.out.println("The object could not found");
				in.close();
				return didntWork;	// -1 flags that it didnt't work
			}
		}catch (IOException ioe){
			System.out.println("Sorry, no saved game was found.");
			return didntWork;		// -1 flags that it didn't work
		}
		
		// delete the content of the file (the game has been restored, so the information in the file is not needed anymore)
		try{
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
			try{
				out.writeChar(' ');	// delete all the contents of the file (replace them with a space char)
				out.close();
				return roundAndNextPlayer;			// SUCCESS !!!!!
			} catch(NotSerializableException nse){
				System.out.println("this object cannot be serialized");
				out.close();
				return didntWork;
			}
		}catch (IOException ioe){
			System.out.println("The file " + file + " could not be opened");
			return didntWork;
		}
	}

	/**
	 * this method resets the players and the number of players. This method is called when a paused game is resumed.
	 */
	private void resetPlayers(Player[] restoredPlayers) {
		players = restoredPlayers;
		numOfPlayers = players.length;
	}

	
	/*-------------------------------------------------------------------------------------------------------
	 * 								METHODS FOR GAME FLOW OPERATIONS
	 * ------------------------------------------------------------------------------------------------------
	 */
	
	/**
	 * this method displays the main menu and asks the player to select an option
	 * 
	 * @return: the menu option that the user selected (int)
	 */
	public int displayMainMenu(Scanner sc) {
		
		int menuChoice = -1;
		boolean validInput = false;
		String errorMessage = "This is not an option. Please try again: ";
		
		System.out.println("\nMain Menu:");
		System.out.println(" Play new game...................1");
		System.out.println(" Restart paused game.............2");
		System.out.println(" Yatzy Rules.....................3");
		System.out.println(" High scores.....................4");
		System.out.println(" Quit the game...................5");
		System.out.print("Your choice: ");
		
		while(!validInput){
			try{
				menuChoice = sc.nextInt();
				if(menuChoice > 0 && menuChoice < 6)
					validInput = true;
				else
					System.out.println(errorMessage);	
			}catch(InputMismatchException e){
				System.out.print(errorMessage);
			}
		}
		return menuChoice;
	}
	
	/**
	 * this method is called at the end of each round and asks the player if he/she wants to continue or exit the game
	 * 
	 * @return: true if the player wants to continue, false if the player decides to quit
	 */
	public boolean continueTheGame(Scanner sc) {
		
		System.out.print("\nPress \"C\" to continue the game or \"E\"to exit/pause the game: ");
	
		String continueGame = sc.next().toUpperCase();
		while(! (continueGame.equals("C") || continueGame.equals("E"))){
			System.out.print("This is not a valid option. Please try again:");
			continueGame = sc.next().toUpperCase();
		}
		// return true if the player inputs C, otherwise false
		return (continueGame.equals("C")) ? true : false;
	}

	
	/**
	 * this method is called at the end of the game.
	 * If the player quits at the end of the game (saveOptionEnabled = false), it just displays a little message, closes the scanner and shuts down the program.
	 * If the player quits during the game, the save option is enabled and the player is asked if he/she wants to save the game. If yes, a method is called to save the game.
	 */
	public void exitGame(Scanner sc, int round, int currentPlayer, boolean saveOptionEnabled) {

		displayLine();
		
		if(saveOptionEnabled){
			System.out.print("Would you like to save the current game? (Y/N)");
			String saveOption = sc.next().toUpperCase();
			while(! (saveOption.equals("N") || saveOption.equals("Y"))){
				System.out.print("This is not an option. Please try again:");
				saveOption = sc.next().toUpperCase();
			}
			if(saveOption.equals("Y")){
				System.out.print("Saving game");
				displayProgressBar();
				if(saveGameState(getPlayers(), round, currentPlayer))
					System.out.println("Game was saved successfully!");
				else
					System.out.println("Sorry, there was a problem. The game could not be saved.");
			}
		}
		System.out.println("\n\t     Thank you for playing Yatzy. Goodbye!");
		displayDoubleLine();
		sc.close();		// close the Scanner
		System.exit(0);	// shut down the JVM
	}

	
	/**
	 * this method establishes the winner by comparing the totals on the player's gamecards. Then it displays the winner on screen.
	 * 
	 * @return: the winning player's index in the player array (int)
	 */
	public int displayWinner() {
		
		int winner = - 1;
		int max = -1;
		
		// find the highest amount of points
		for(int i = 0; i < getNumOfPlayers(); i++){
			if(getPlayers()[i].getGameCard(15) > max){
				max = getPlayers()[i].getGameCard(15);
				winner = i;
			}
		}
		System.out.println("\n\t\t" + getPlayers()[winner].getName().toUpperCase() + " won the game with " + max + " points.\n");
		return winner;
	}

	/**
	 * this method displays the amount of wins per player
	 */
	public void displayStatistics(int winnerIndex) {
		
		getPlayers()[winnerIndex].updateNoOfWins();	// first update the players wins
		System.out.println("\nStatistics:");
		System.out.println("-----------");
		for(int i = 0; i < getNumOfPlayers(); i++){
			System.out.print("Wins for " + getPlayers()[i].getName() + ":");
			if(getPlayers()[i].getName().length() < 6)
				System.out.print("\t");	// add extra tab for short names
			System.out.print("\t " + getPlayers()[i].getNoOfWins() + "\n");
		}
	}

	/**
	 * this method displays a summary of the game (full game card)
	 */
	public void displaySummary(Scanner sc){
		
		System.out.print("Press any letter or number to display a summary of the game: ");
		sc.next();	// get input
		System.out.println("\nSummary of the game:");
		// display card
		displayCard();
	}
}
