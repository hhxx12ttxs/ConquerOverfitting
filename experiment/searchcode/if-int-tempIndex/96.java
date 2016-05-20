import java.util.*;
import java.io.*;


public class EvilHangMan extends AbstractHungman implements HangmanGame {
	
	private String[] wordlist = new String[235000];// to store the dictionary
	private int numWords = 0;// count the number of possible secret words.
	private int secretStringLength;// the length of the secret string
	private boolean guessResult = false;

	public EvilHangMan(int StringLength, int numGuesses) {
		guessesRemaining = numGuesses;
		secretStringLength = StringLength;
		Scanner scanner = null;
		try {
			scanner = new Scanner(new File("dictionary.txt"));// read the dictionary
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		int i = 0;
		while (scanner.hasNext()) {
			String temp = scanner.nextLine().toUpperCase();
			if (temp.length() == StringLength) {
				wordlist[i] = temp;
				i++;
				numWords++;
			}
		}

		for (i = 0; i < StringLength; i++) {
			currentState += "_ ";
		}
		scanner.close();

	}

	

	public int numGuessesRemaining() {
		return guessesRemaining;
	}

	public int numLettersRemaining() {
		return 26; // because they never get one right!
	}
	
	@Override
	public boolean isWin() {
		return false;
	}
	
	@Override
	public boolean gameOver() {
		if (guessesRemaining == 0)
			return true;
		else
			return false;
	}


	public boolean makeGuess(char ch) {

		System.out.println("makeGuess: " + ch + "; numWords=" + numWords);
		guessResult = false;
		guess = ch;
		if (Character.isLetter(ch) && !alreadyGuessed(ch)) {
			// adjust the Wordlist in order to avoid the word with the letter
			// user guessed
			int tempWordNum = 0;
			for (int i = 0; i < numWords; i++) {
				for (int j = 0; j < secretStringLength; j++) {
					if (wordlist[i].charAt(j) == ch) {
						break;
					} else {
						if (j == secretStringLength - 1) {
							if (wordlist[i].charAt(j) != ch) {
								tempWordNum++;
							}
						}
					}
				}
			}
			// we choose the words that don't contain the letter the user
			// guessed, and they will be the new possible secret words.
			String[] temp = new String[tempWordNum];
			int tempIndex = 0;
			for (int i = 0; i < numWords; i++) {
				for (int j = 0; j < secretStringLength; j++) {
					if (wordlist[i].charAt(j) == ch) {
						break;
					} else {
						if (j == secretStringLength - 1) {
							if (wordlist[i].charAt(j) != ch) {
								temp[tempIndex] = wordlist[i];
								tempIndex++;
							}
						}
					}
				}
			}
			if (tempWordNum == 0) {
				System.out.println("tempWordNum is zero!");

				originSecretWord = wordlist[0];
				guessResult = true;
			} else {
				originSecretWord = temp[0];
				numWords = tempWordNum;
				wordlist = temp;
				guessesRemaining--;
				guessResult = false;
			}
			if (!guessResult) {
				history = history + guess;
			}

		} else return false;
		
		return guessResult;
	}

 
}
