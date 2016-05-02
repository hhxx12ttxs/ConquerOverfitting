
package vjl.core.tools;




public class Strings
		extends Tools
{
	public static final boolean equals (final String first, final String second)
	{
		return ((first != null) ? first.equals (second) : (second == null));
	}
	
	public static final char upperCase (final char character)
	{
		return (Character.toUpperCase (character));
	}
	
	public static final String select (final String string, final int offset)
	{
		Enforcements.nonNull (string);
		return (string.substring (offset));
	}
	
	public static final boolean areEqual (final String first, final String second)
	{
		final boolean equal;
		if (first == second)
			equal = true;
		else if ((first == null) || (second == null))
			equal = false;
		else
			equal = first.equals (second);
		return (equal);
	}
	
	public static final String concatenate (final Object ... parts)
	{
		final StringBuilder builder = new StringBuilder ();
		for (final Object part : parts)
			builder.append (part);
		return (builder.toString ());
	}
	
	public static final String join (final String prefix, final String infix, final String sufix, final String ... parts)
	{
		final StringBuilder builder = new StringBuilder ();
		builder.append (prefix);
		final int count = parts.length;
		if (count > 0) {
			for (int index = 0; index < (count - 1); index++) {
				builder.append (parts[index]);
				builder.append (infix);
			}
			builder.append (parts[count - 1]);
		}
		builder.append (sufix);
		return (builder.toString ());
	}
	
	public static final String join (final String prefix, final String infix, final String sufix, final String part, final int count)
	{
		final StringBuilder builder = new StringBuilder ();
		builder.append (prefix);
		if (count > 0) {
			for (int index = 0; index < (count - 1); index++) {
				builder.append (part);
				builder.append (infix);
			}
			builder.append (part);
		}
		builder.append (sufix);
		return (builder.toString ());
	}
	
	public static final String format (final int value)
	{
		return (Integer.toString (value));
	}
	
	public static final String format (final double value)
	{
		return (Double.toString (value));
	}
	
	public static final String formatAsHex (final byte bite)
	{
		final String string;
		if (bite >= 0)
			string = Strings.hexBytes[bite];
		else
			string = Strings.hexBytes[256 + bite];
		return (string);
	}
	
	public static final String formatAsHex (final byte ... bytes)
	{
		final StringBuilder builder = new StringBuilder ();
		for (final byte bite : bytes)
			if (bite >= 0)
				builder.append (Strings.hexBytes[bite]);
			else
				builder.append (Strings.hexBytes[256 + bite]);
		return (builder.toString ());
	}
	
	public static final char formatAsPrintable (final byte bite)
	{
		final char printable;
		if (bite >= 0)
			printable = Strings.printableBytes[bite];
		else
			printable = Strings.printableBytes[256 + bite];
		return (printable);
	}
	
	public static final String formatAsHex16 (final int value)
	{
		return (new String (new char [] {
				Strings.hexDigits[(value & 0xf000) >> 12],
				Strings.hexDigits[(value & 0x0f00) >>  8],
				Strings.hexDigits[(value & 0x00f0) >>  4],
				Strings.hexDigits[(value & 0x000f)      ]}));
	}
	
	public static final String formatAsHex32 (final int value)
	{
		return (new String (new char [] {
				Strings.hexDigits[(value & 0xf0000000) >> 28],
				Strings.hexDigits[(value & 0x0f000000) >> 24],
				Strings.hexDigits[(value & 0x00f00000) >> 20],
				Strings.hexDigits[(value & 0x000f0000) >> 16],
				Strings.hexDigits[(value & 0x0000f000) >> 12],
				Strings.hexDigits[(value & 0x00000f00) >>  8],
				Strings.hexDigits[(value & 0x000000f0) >>  4],
				Strings.hexDigits[(value & 0x0000000f)      ]}));
	}
	
	public static final boolean isLetter (
			final char character)
	{
		return (Character.isLetter (character));
	}
	
	private static final char[] hexDigits = {
			'0', '1', '2', '3',
			'4', '5', '6', '7',
			'8', '9', 'a', 'b',
			'c', 'd', 'e', 'f'};
	
	private static final String[] hexBytes;
	
	private static final char[] printableBytes;
	
	static {
		
		hexBytes = new String [256];
		for (int bite = 0; bite < 256; bite++) {
			final int highPart = bite >> 4;
			final int lowPart = bite & 0xf;
			hexBytes[bite] = new String (
					new char[] {Strings.hexDigits[highPart], Strings.hexDigits[lowPart]}) .intern ();
		}
		
		printableBytes = new char [256];
		for (int bite = 0; bite < 256; bite++)
			if ((bite >= 32) && (bite <= 127))
				printableBytes[bite] = (char) bite;
			else
				printableBytes[bite] = ' ';
	}
}

