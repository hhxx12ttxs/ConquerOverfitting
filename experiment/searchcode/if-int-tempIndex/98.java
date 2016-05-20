import java.util.*;
import java.io.*;

public class evilHangMan implements HangmanGame {
	private String SecretWord = "";// To store the secret word
	private int GuessRemainingNum;// to store the number of guess for the user
	private String CurrentState = "";// store the current guessing situation
	private String LetterGuessHistory = "";// store the letter user has tried
	private char LetterGuess;// the letter the user guess right now
	private String[] Wordlist = new String[235000];// to store the dictionary
	private int WordNum = 0;// count the number of possible secret words.
	private int SecretString_length;// the length of the secret string
	private int LetterLeftNum = 26;// the value in evilhangman is not used since
									// the user will always guess wrongly.
	private boolean GuessResult = false;

	public evilHangMan(int StringLength, int numGuesses) {
		GuessRemainingNum = numGuesses;
		SecretString_length = StringLength;
		Scanner S = null;
		try {
			S = new Scanner(new File("dictionary.txt"));// read the dictionary
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		int i = 0;
		while (S.hasNext()) {
			String temp = S.nextLine().toUpperCase();
			if (temp.length() == StringLength) {
				Wordlist[i] = temp;
				i++;
				WordNum++;
			}
		}

		for (i = 0; i < StringLength; i++) {
			CurrentState += "_ ";
		}
		S.close();
	}

	public String getSecretWord() {
		return SecretWord;
	}

	public int numGuessesRemaining() {
		return GuessRemainingNum;
	}

	public int numLettersRemaining() {
		return LetterLeftNum;
	}

	public boolean isWin() {
		return false;
	}

	public boolean gameOver() {
		if (GuessRemainingNum == 0)
			return true;
		else
			return false;
	}

	public String lettersGuessed() {
		if (!GuessResult) {
			LetterGuessHistory = LetterGuessHistory + LetterGuess;
		}
		return LetterGuessHistory;
	}

	public String displayGameState() {
		return CurrentState;
	}

	public boolean EvilChooseString(char ch) {
		// adjust the Wordlist in order to avoid choose the word with the letter
		// user guessed
		int tempWordNum = 0;
		for (int i = 0; i < WordNum; i++) {
			for (int j = 0; j < SecretString_length; j++) {
				if (Wordlist[i].charAt(j) == ch) {
					break;
				} else {
					if (j == SecretString_length - 1) {
						if (Wordlist[i].charAt(j) != ch) {
							tempWordNum++;
						}
					}
				}
			}
		}
		// we choose the words which doesn't contain the letter the user
		// guessed, and they will be the new possible secret words.
		String[] temp = new String[tempWordNum];
		int tempIndex = 0;
		for (int i = 0; i < WordNum; i++) {
			for (int j = 0; j < SecretString_length; j++) {
				if (Wordlist[i].charAt(j) == ch) {
					break;
				} else {
					if (j == SecretString_length - 1) {
						if (Wordlist[i].charAt(j) != ch) {
							temp[tempIndex] = Wordlist[i];
							tempIndex++;
						}
					}
				}
			}
		}
		if (tempWordNum == 0) {

			SecretWord = Wordlist[0];
			return false;
		} else {
			SecretWord = temp[0];
			WordNum = tempWordNum;
			Wordlist = temp;
			return true;
		}
	}

	public boolean makeGuess(char ch) {
		GuessResult = false;
		LetterGuess = ch;
		if (!RepeatInput()) {
			if (EvilChooseString(LetterGuess))// if we can adjust the possible
												// secret words
			{
				GuessRemainingNum--;
				GuessResult = false;
			} else {
				GuessResult = true;
			}
		} else// if the user input some letter as the same as he guessed before,
				// the user should try another letter.
		{
			System.out.println("Please don't make a same guess");
			System.out.print("Next guess: ");
			Scanner S2 = new Scanner(System.in);
			String input = "";
			input = S2.nextLine().toUpperCase(); // ensure all guesses converted
													// to upper case
			System.out.println(); // force the line break
			char ch2 = input.charAt(0);
			GuessResult = makeGuess(ch2);
			S2.close();
		}
		return GuessResult;
	}

	public boolean RepeatInput() {
		return false;
	}
}
