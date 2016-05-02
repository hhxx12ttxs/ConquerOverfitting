package edu.ucsb.cs56.projects.math.conversion_quiz;

import java.lang.Math;

/**
 * A class to represent a Question in the conversion quiz
 * Original program by Erick Valle & George Barrios for Mantis 0000391
 * @author Andrew Berls
 * @author Brian Wan, Fanny Kuang
 * @version CS56, Spring 2013, GitHub
 */

public class Question {

	private int num;

	private String strRadix; // The radix represented as a string, e.g. "Binary"

	private int intRadix;
    
        private int num2; //Masking 2nd number

	/**
	 * Construct a question with a random number and radix
	 */
	public Question() {
		this.num = this.getRandomNum();
		this.setRadixes(this.getRandomRadix());
	}
	
	/**
	 * Construct a question with a set radix but random number
	 * @param radix The radix to set as an integer
	 */
	public Question(int radix) {
		int num = this.getRandomNum();
		this.num = num;
		this.setRadixes(radix);
	}
	
	/**
	 * Construct a question with a specified number and conversion radius
	 * @param num the number to convert
	 * @param radix the radix to convert to
	 */
	public Question(int num, int radix) {
		this.num = num;
		this.setRadixes(radix);
	}

	public String getStrRadix() {		
		return getRadixString(this.intRadix);
	}

	public void setStrRadix(String radix) {
		this.strRadix = radix;
	}
	
	/**
	 * Setter for string radix converts integer to appropriate string
	 * @param radix The radix as an integer
	 */
	public void setStrRadix(int radix) {
		this.strRadix = getRadixString(radix);
	}

	public int getNum() {
		return this.num;
	}

	public void setNum(int x) {
		this.num = x;
	}

	public int getIntRadix() {
		return this.intRadix;
	}

	public void setIntRadix(int x) {
		this.intRadix = x;
	}
	
	/**
	 * Set both the int and string radixes
	 * @param radix the radix to set(as an int)
	 */
	public void setRadixes(int radix) {
		this.setIntRadix(radix);
		this.setStrRadix(radix);
	}

	/**
	 * Return a random integer between 0 and 99
	 */
	public int getRandomNum() {		
		return (int) (Math.random() * 100);
	}
	
	/**
	 * Randomly return 2, 8, 10, or 16
	 * @return a randomly selected radix
	 */
	public int getRandomRadix() {
		int rand = (int) (Math.random() * 5); // 0-4
		int result = -1;
		switch(rand) {
		case 0:
			result = 2;
			break;
		case 1:
			result = 8;
			break;
		case 2:
			result = 10;
			break;
		case 3:
			result = 16;
			break;
		case 4://to get a mask question
		    result = 18;
		    break;
		}
		return result;
	}
	
	/**
	 * Take an integer radix and return its corresponding String representation
	 * @return The radix string
	 */
	public String getRadixString(int radix) {
		String result = "";
		switch (radix) {
		case 2:
			result = "Binary";
			break;
		case 8:
			result = "Octal";
			break;
		case 10:
			result = "Decimal";
			break;
		case 16:
			result = "Hexadecimal";
			break;
	        case 18: //mask question
		    result = "mask";
		    break;
		}
		return result;
	}

	/**
	 * Generate a prompt string using a random number and representation radix
	 * @return the complete question prompt
	 */
	public String generatePrompt() {
		// Set a 'from' radix that is not equal to the 'to' radix,
		// which is contained in this.intRadix
		int radix = this.getRandomRadix();
		while (radix == this.getIntRadix()) radix = this.getRandomRadix();		
		
		String num = Integer.toString(this.num, radix);
		String srcRadix = getRadixString(radix);
		//MASK Question
		if (radix == 18 || this.getStrRadix() == "mask"){
		    intRadix = 18;
		    String number = Integer.toString(this.num, 2);
		    num2 = this.getRandomNum();
		    String number2 = Integer.toString(num2, 2);
		    return String.format("What is the binary output when applying the mask %s to %s?", number, number2);
		}
		return String.format("Convert %s from %s to %s:", num, srcRadix, this.getStrRadix()); 
	}
	
	/**
	 * Practice converting random radixes to a specific radix
	 * @param destRadix The radix to convert to
	 */
	public String generatePrompt(int destRadix) {
		
		if (destRadix == -1) {
			// In the GUI, quiz.mode is passed in which specifies a destination radix
			// -1 indicates random mode, so we in that case we just generate a prompt from the
			// non-overloaded method
			return this.generatePrompt();
		} else {
			// Set a 'from' radix that is not equal to the 'to' radix
			int srcRadix = this.getRandomRadix();
			while (srcRadix == destRadix) srcRadix = this.getRandomRadix();
			
			String num = Integer.toString(this.num, srcRadix);
			
			return String.format("Converts %s from %s to %s:", num, getRadixString(srcRadix), this.getRadixString(destRadix));
		}
	}
	
	/**
	 * Strip all leading zeroes, spaces, and convert to lowercase
	 * @param s - the string to sanitize
	 * @return the sanitized string
	 */
	public String sanitize(String s) {
	  return s.replaceAll("^0*", "").replaceAll(" ", "").toLowerCase();
	}
	
	/**
	 * Return a string representation of the number in the specified radix
	 * @param radix The radix to convert to
	 * @return Number as a string
	 */
	public String convertTo(int radix) {
	    if (radix == 18){
		Integer mask = this.num & num2;
		String answer = Integer.toString(mask,2);
		return Integer.toString(mask, 2);
	    }
		return Integer.toString(this.getNum(),radix);
	}
	
	/**
	 * Test if a user submitted answer matches the correct conversion
	 * Leading zeroes are stripped for flexibility
	 * @param userAnswer The string answer submitted by the user
	 */
	public boolean checkAnswer(String userAnswer) {
	        userAnswer = sanitize(userAnswer);
		String answer = this.convertTo(this.getIntRadix());
		return (userAnswer.equals(answer)) ? true : false;
		
	}
	
	/**
	 * Return the correct conversion as a string
	 * @return The number converted to the radix (as a string)
	 */
	public String getAnswer() {
	    if (this.getIntRadix() == 18){ return Integer.toString(this.num & num2, 2);}
		return Integer.toString(this.num, this.intRadix);
	}
	
}

