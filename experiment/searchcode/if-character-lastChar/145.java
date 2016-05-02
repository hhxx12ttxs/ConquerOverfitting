package affd.logic;

import java.util.ArrayList;

public class PhoneNumberAnonymizer implements Anonymizer {
	
	public static String ANONYMIZED_LETTER = "*";
	
	public static final String MODIFICATION_CLASS = "phonenumber";
	
	public static final int MIN_LENGTH = 8;
	public static final int MAX_LENGTH = 20;
	public static final int MAX_PHONE_TOKEN_LENGTH = 10;
	
	private ArrayList<Token> candidatePhone = null;
	private int candidatePhoneTokenSize = 0;
	String groupID = null;

	/**
	 * Constructor for PhoneNumberAnonymizer
	 */
	public PhoneNumberAnonymizer() {
	}
	

	/**
	 * Replaces all the phone numbers in the phone number candidate list.
	 */
	private void replaceAll() {
		groupID = Token.generateGroupId();
		for (Token token : candidatePhone)
			anonymize(token);
	}
	
	@Override
	public void process(ArrayList<Sentence> sentences) throws LogicException {
		// Process only unknown tokens
		for(Sentence sentence : sentences) {
			ArrayList<Token> tokens = sentence.getTokens();
			Token token;
			for (int i = 0; i < tokens.size(); i++) {
				token = tokens.get(i);
				// Check token
				if(isPhoneNumber(token)) {
					replaceAll();
					// point to the token next to the last token in the phone number
					i += candidatePhoneTokenSize;
				}
			}
		}		
	}
	
	/**
	 * Checks if the given character is one of the accepted punctuation marks in between 
	 * phone number parts.
	 * 
	 * @param c Character which is checked if it is a punctuation mark or not
	 * @return Boolean value which tells if the given character was a punctuation mark
	 */
	private boolean isPunctuation(char c) {
		if (c == ',' || c == '.' || c == '!' || c == '?' || c == ';' || c == ':')
			return true;
		return false;
	}

	/**
	 * Checks if all the characters in the possible phone number are valid marks.
	 * 
	 * @param token Token which might contain the phon number
	 * @return Boolean value which tells if the token is found to be a phone number of not
	 */
	private boolean isValidSignes(String token) {
		int i = 0;
		
		if (token.length() < 1) {
			return false;
		}
		
		if (token.charAt(0) == '+') {
			i = 1;
		}
		
		char lastChar = token.charAt(token.length() - 1);
		if (isPunctuation(lastChar))
			token = token.substring(0, token.length() - 1);

		for(; i < token.length(); i++) {
			if (!(Character.isDigit(token.charAt(i)) || token.charAt(i) == ' ' || token.charAt(i) == '-'))
				return false;
		}
		
		return true;
	}
	
	/**
	 * Checks through the token to see if it is probably a phone number or not. 
	 * 
	 * @param token The Token to process which might be a phone number
	 * @return The compiled phone number in a String or null if the Token is not
	 * a phone number
	 */
	private String compilePhone(Token token) {
		candidatePhoneTokenSize = 0;
		candidatePhone = null;
		ArrayList<Token> candidates = new ArrayList<Token>();
		StringBuffer buffer = new StringBuffer();
		Token curToken = token;
		int counter = 0;
		
		char lastChar;
		
		while (curToken != null && 
				(curToken.getType() == Token.Type.UNKNOWN || 
				curToken.getType() == Token.Type.WHITE_SPACE ||
				curToken.getType() == Token.Type.PUNCTUATION)) {
			candidatePhoneTokenSize++;
			
			// there are too many numbers!
			if (counter > MAX_PHONE_TOKEN_LENGTH)
				return null;
			if (curToken.getType() == Token.Type.WHITE_SPACE)
				;
			else if (isValidSignes(curToken.getContent().trim())) {
				buffer.append(curToken.getContent());
				candidates.add(curToken);
				lastChar = curToken.getContent().charAt(curToken.getContent().length() - 1);
				// if phone ends to the punctuation mark, it is a complete phone number
				if (isPunctuation(lastChar))
					break;
			}
			curToken = curToken.getNextToken();
			counter++;
		}
		candidatePhone = candidates;
		return buffer.toString();
	}
	
	/**
	 * Checks if all the characters in a phone number candidate are valid
	 * content for a phone number.
	 * 
	 * @param candidate A string of which characters are checked
	 * @return Boolean value which tells if all the characters in the string were
	 * valid characters for being a phone number
	 */
	private boolean isValidContent(String candidate) {
			
		int i = 0;
		if (!Character.isDigit(candidate.charAt(0)) && candidate.charAt(0) != '+')
			return false;
		if (candidate.charAt(0) == '+')
			i = 1;
		char lastChar = candidate.charAt(candidate.length() - 1);
		if (isPunctuation(lastChar))
			candidate = candidate.substring(0, candidate.length() - 1);
			
		if (MIN_LENGTH < candidate.length() && candidate.length() < MAX_LENGTH) {
			for (; i < candidate.length(); i++) {
				//System.out.print("i: " + candidate.charAt(i) + ", ");
				if (!Character.isDigit(candidate.charAt(i)) && candidate.charAt(i) != '-')
					return false;
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Checks if the Token is a phone number.
	 * 
	 * @param token Token to examine
	 * @return Boolean value which tells if the token is most probably a phone number or not.
	 */
	private boolean isPhoneNumber(Token token) {
		if(token.getType() == Token.Type.UNKNOWN && isValidSignes(token.getContent().trim())) {
			//System.out.println("Potential Phone: " + token.getContent());
			String candidate = compilePhone(token);
			if (candidate != null && isValidContent(candidate.trim()))
				return true;
		}
		return false;
	}
	
	@Override
	public void anonymize(Token token) {
//		System.out.println("Anonymized: " + token.getContent());
		
		int tokenLength;
		boolean wasPunctuationAtTheEnd = false;
		char lastChar = token.getContent().charAt(token.getContent().length() - 1);
		if (isPunctuation(lastChar)) {
			tokenLength = token.getContent().length() - 1;
			wasPunctuationAtTheEnd = true;
		}
		else
			tokenLength = token.getContent().length();
		
		StringBuffer contentBuffer = new StringBuffer();
		for(int i = 0; i < tokenLength; i++) {
			contentBuffer.append(PhoneNumberAnonymizer.ANONYMIZED_LETTER);
		}
		if (wasPunctuationAtTheEnd)
		contentBuffer.append(token.getContent().charAt(token.getContent().length() - 1));
		
		token.modifyContent(contentBuffer.toString(), MODIFICATION_CLASS, groupID);
		
	}	
}
