package edu.vub.at.doc;

/**
 * XAmbientTalkParse is thrown when illegal input is parsed by the AmbientTalk parser.
 * 
 * @author bcorne
 */
public class XAmbientTalkParseError extends Exception {

	private static final long serialVersionUID = 3910243526234096497L;
	
	/** Entire input String on which the parser failed */
	private final String input;
	
	/** Line on which the parser failed */
	private final int line;
	
	/** Column on which the parser failed */
	private final int column;
	
	/**
	 * Complete constructor taking information on both the error and the precise position in which
	 * the erroneous code was detected by the parser.
	 * @param message a message detailing the suspected reason for the parser to fail
	 * @param input the file from which the parser was reading
	 * @param line the line at which the erroneous code is situated
	 * @param column the column at which the token that could not be parsed starts.
	 * @param cause an underlying exception detailing the actual cause of this exception
	 */
	public XAmbientTalkParseError(
			String message,
			String input,
			int line,
			int column,
			Throwable cause) {
		super(message, cause);
		this.input = input;
		this.line = line;
		this.column = column;
	}

	/**
	 * Method overridden to provide a more elaborate message when information on the source file,
	 * the line number and column position were provided.
	 */
	public String getMessage() {
		if (input != null) {
			return "(l"+line+":c"+column+") on input\n"+input+"-------\n"+super.getMessage();
		} else {
			return super.getMessage();
		}
	}

	// Generated getters and setters
	public String getInput() {
		return input;
	}

	public int getLine() {
		return line;
	}

	public int getColumn() {
		return column;
	}
		
}

