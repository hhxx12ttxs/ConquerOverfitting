/* CSE 142 Autumn 2010     Sean Marihugh	Section AD	11/2/10
This is the Guessing Game program from Homework 4 which shows an understanding of
user inputs and returns in an interactive program in which the user guesses a number
and gets statistics printed at the end.  */

import java.util.*;// so that I can use Scanner

public class GuessingGame	{
	public static final int MAX = 100; //sets a constant to change the range of the game
	
	public static void main (String[] args)	{
		Scanner console = new Scanner(System.in); //declares user input
		
		haiku();
		
		int sum;
		boolean yes = true;		
		int games = 0;				//lines 14-16 declare variables to be passed
		int guesses = 0;			//into the methods and loops below.
		int best = MAX;
		while (yes)	{			//loops the game when the answer is "yes"
			sum = guess(console);
			yes = playAgain(console);
			games++;
			guesses += sum;
			if (guesses < best)	{ //establishes best game calculation
				best = sum;
			}
		}
		
		results(games, guesses, best);
	}
	
	public static void haiku()	{ //creates the 3-line haiku
		System.out.println("This game is quite fun,");
		System.out.println("Enter a number and wait,");
		System.out.println("And see if you win.");
	}
	
	public static int guess(Scanner console)	{	//inserts user input for the number guesses
		Random randy = new Random();
		int max = randy.nextInt(MAX) + 1;
		int guess = 0;
		int sum = 0;
		System.out.println();
		System.out.println("I'm thinking of a number between 1 and  " + MAX + "...");

		while (guess != max)	{	//loops the guesses until user reaches correct number
			System.out.print("Your guess? ");
			guess = console.nextInt();
			if (guess < max)	{	
				System.out.println("It's higher.");
			} else if( guess > max)	{
				System.out.println("It's lower.");
			}
			sum++;
		}
		
		if (sum == 1)	{	//deals with special case of single guesses
			System.out.println("You got it right in " + sum + " guess!");
		} else	{
			System.out.println("You got it right in " + sum + " guesses!");
		}
		return sum;
	}
	
	public static boolean playAgain(Scanner console)	{	//asks the user if he wants to play again
		System.out.print("Do you want to play again? ");	//and returns a boolean value for use in
		String response = console.next().toLowerCase();		//the main.
		String ans = response.substring(0, 1);
		if (ans.equals("y"))	{
			return true;
		}
		return false;
	}

	public static void results(int games, int guesses, int best)	{	//prints the results table
		System.out.println();														//uses variables declared in
		System.out.println("Overall results: ");								//the main.
		System.out.println("Total games   = " + games);
		System.out.println("Total guesses = " + guesses);
		System.out.printf("Guesses/game  = %.2f\n", (double) guesses / games);
		System.out.println("Best game     = " + best);
	}
 }

